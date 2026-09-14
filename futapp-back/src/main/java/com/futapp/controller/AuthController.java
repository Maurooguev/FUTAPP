package com.futapp.controller;
import com.futapp.dto.*;
import com.futapp.model.Usuario;
import com.futapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.futapp.service.LoginAttemptService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final LoginAttemptService loginAttempts;

    public AuthController(AuthService auth, LoginAttemptService loginAttempts) { this.auth = auth; this.loginAttempts = loginAttempts; }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) { return auth.register(request); }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest servletRequest) {
        String key = servletRequest.getRemoteAddr() + ":" + request.email().trim().toLowerCase();
        loginAttempts.verify(key);
        try { AuthResponseDTO response = auth.login(request); loginAttempts.succeeded(key); return response; }
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
    public void logout(@RequestHeader("Authorization") String header) {
        auth.logout(header.substring("Bearer ".length()));
    }
}
