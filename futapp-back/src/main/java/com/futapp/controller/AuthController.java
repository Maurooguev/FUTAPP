package com.futapp.controller;
import com.futapp.dto.*;
import com.futapp.model.Usuario;
import com.futapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.futapp.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final LoginAttemptService loginAttempts;
    private final Duration sessionDuration;
    private final boolean secureCookie;
    private final boolean exposeToken;

    public AuthController(AuthService auth, LoginAttemptService loginAttempts,
                          @Value("${futapp.auth.session-duration:7d}") Duration sessionDuration,
                          @Value("${futapp.auth.cookie-secure:false}") boolean secureCookie,
                          @Value("${futapp.auth.expose-token:false}") boolean exposeToken) {
        this.auth = auth;
        this.loginAttempts = loginAttempts;
        this.sessionDuration = sessionDuration;
        this.secureCookie = secureCookie;
        this.exposeToken = exposeToken;
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request, HttpServletResponse response) {
        return establishSession(auth.register(request), response);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest servletRequest,
                                 HttpServletResponse response) {
        String key = request.email().trim().toLowerCase();
        loginAttempts.verify(key);
        try { AuthResponseDTO session = auth.login(request); loginAttempts.succeeded(key); return establishSession(session, response); }
        catch (IllegalArgumentException exception) { loginAttempts.failed(key); throw exception; }
    }

    @PostMapping("/password/forgot")
    public PasswordResetResponseDTO forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) { return auth.forgotPassword(request); }

    @PostMapping("/password/reset")
    public PasswordResetResponseDTO resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) { return auth.resetPassword(request); }

    @PutMapping("/password")
    public PasswordResetResponseDTO changePassword(@AuthenticationPrincipal Usuario usuario,
                                                    @Valid @RequestBody ChangePasswordRequestDTO request) {
        return auth.changePassword(usuario, request);
    }

    @GetMapping("/me")
    public UsuarioSesionDTO me(@AuthenticationPrincipal Usuario usuario) { return UsuarioSesionDTO.from(usuario); }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       @RequestHeader(value = "Authorization", required = false) String header) {
        String token = sessionToken(request, header);
        if (token != null) auth.logout(token);
        clearSessionCookie(response);
    }

    @PostMapping("/logout-all")
    public void logoutAll(@AuthenticationPrincipal Usuario usuario, HttpServletResponse response) {
        auth.logoutAll(usuario);
        clearSessionCookie(response);
    }

    private AuthResponseDTO establishSession(AuthResponseDTO session, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("FUTAPP_SESSION", session.token())
                .httpOnly(true).secure(secureCookie).sameSite("Lax")
                .path("/api/v1").maxAge(sessionDuration).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return exposeToken ? session : new AuthResponseDTO(null, session.idUsuario(), session.nombre(),
                session.email(), session.rol(), session.roles());
    }

    private void clearSessionCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("FUTAPP_SESSION", "")
                .httpOnly(true).secure(secureCookie).sameSite("Lax")
                .path("/api/v1").maxAge(Duration.ZERO).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String sessionToken(HttpServletRequest request, String header) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("FUTAPP_SESSION".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }
}
