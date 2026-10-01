package com.futapp.service;

import com.futapp.dto.*;
import com.futapp.model.AuthToken;
import com.futapp.model.PasswordResetToken;
import com.futapp.model.Usuario;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.PasswordResetTokenRepository;
import com.futapp.repository.UsuarioRepository;
import com.futapp.security.TokenSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@Transactional
public class AuthService {
    private final UsuarioRepository usuarios;
    private final AuthTokenRepository tokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder encoder;
    private final PasswordResetMailService passwordResetMail;
    private final boolean exposeResetToken;
    private final Duration sessionDuration;
    private final int maximumSessions;
    private final String registrationInviteCode;
    public AuthService(UsuarioRepository usuarios, AuthTokenRepository tokens,
                       PasswordResetTokenRepository resetTokens, PasswordEncoder encoder,
                       PasswordResetMailService passwordResetMail,
                       @Value("${futapp.password-reset.expose-token:false}") boolean exposeResetToken,
                       @Value("${futapp.auth.session-duration:7d}") Duration sessionDuration,
                       @Value("${futapp.auth.maximum-sessions:5}") int maximumSessions,
                       @Value("${futapp.registration.invite-code:}") String registrationInviteCode) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.resetTokens = resetTokens;
        this.encoder = encoder;
        this.passwordResetMail = passwordResetMail;
        this.exposeResetToken = exposeResetToken;
        if (sessionDuration.isZero() || sessionDuration.isNegative()) throw new IllegalArgumentException("La duración de sesión debe ser positiva");
        if (maximumSessions < 1) throw new IllegalArgumentException("Debe permitirse al menos una sesión por cuenta");
        this.sessionDuration = sessionDuration;
        this.maximumSessions = maximumSessions;
        this.registrationInviteCode = registrationInviteCode == null ? "" : registrationInviteCode;
    }
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (!registrationInviteCode.isBlank() && !secureEquals(registrationInviteCode, request.codigoInvitacion())) {
            throw new IllegalArgumentException("El código de invitación no es válido");
        }
        String email = request.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        String rol = request.rol() == null || request.rol().isBlank() ? "JUGADOR" : request.rol().trim().toUpperCase();
        if (!java.util.Set.of("ORGANIZADOR", "DIRECTOR_EQUIPO", "JUGADOR").contains(rol)) {
            throw new IllegalArgumentException("Tipo de cuenta inválido");
        }
        Usuario user = new Usuario(); user.setNombre(request.nombre().trim()); user.setEmail(email); user.setPasswordHash(encoder.encode(request.password())); user.setRol(rol);
        return createToken(usuarios.save(user));
    }
    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario user = usuarios.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw new IllegalArgumentException("Email o contraseña incorrectos");
        return createToken(user);
    }
    public PasswordResetResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        final String[] generatedToken = new String[1];
        usuarios.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
            resetTokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
            String token = TokenSecurity.generate();
            PasswordResetToken reset = new PasswordResetToken();
            reset.setTokenHash(TokenSecurity.hash(token));
            reset.setUsuario(user);
            reset.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            resetTokens.save(reset);
            passwordResetMail.send(user, token);
            generatedToken[0] = token;
        });
        return new PasswordResetResponseDTO(
                "Si el email está registrado, generamos un enlace válido durante 30 minutos.",
                exposeResetToken ? generatedToken[0] : null);
    }
    public PasswordResetResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken reset = resetTokens.findByTokenHashAndUsedAtIsNull(TokenSecurity.hash(request.token()))
                .orElseThrow(() -> new IllegalArgumentException("El enlace de recuperación no es válido"));
        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalArgumentException("El enlace de recuperación venció");
        Usuario user = reset.getUsuario();
        user.setPasswordHash(encoder.encode(request.nuevaPassword()));
        usuarios.save(user);
        reset.setUsedAt(LocalDateTime.now());
        resetTokens.save(reset);
        tokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
        return new PasswordResetResponseDTO("Contraseña restablecida. Ya podés iniciar sesión.", null);
    }
    public PasswordResetResponseDTO changePassword(Usuario user, ChangePasswordRequestDTO request) {
        if (!encoder.matches(request.passwordActual(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        if (encoder.matches(request.nuevaPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña nueva debe ser diferente");
        }
        user.setPasswordHash(encoder.encode(request.nuevaPassword()));
        usuarios.save(user);
        tokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
        resetTokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
        return new PasswordResetResponseDTO("Contraseña actualizada. Volvé a iniciar sesión.", null);
    }
    public void logout(String token) {
        String tokenHash = TokenSecurity.hash(token);
        if (tokens.existsById(tokenHash)) tokens.deleteById(tokenHash);
        else tokens.deleteById(token); // Compatibilidad temporal con sesiones creadas antes del hash.
    }
    public void logoutAll(Usuario user) {
        tokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
    }
    private AuthResponseDTO createToken(Usuario user) {
        String rawToken = TokenSecurity.generate();
        AuthToken authToken = new AuthToken();
        authToken.setToken(TokenSecurity.hash(rawToken));
        authToken.setUsuario(user);
        authToken.setExpiresAt(LocalDateTime.now().plus(sessionDuration));
        tokens.save(authToken);
        var userTokens = tokens.findByUsuarioIdUsuarioOrderByExpiresAtDesc(user.getIdUsuario());
        if (userTokens.size() > maximumSessions) {
            tokens.deleteAll(userTokens.subList(maximumSessions, userTokens.size()));
        }
        return new AuthResponseDTO(rawToken, user.getIdUsuario(), user.getNombre(), user.getEmail(), user.getRol(), user.getRoles());
    }
    private boolean secureEquals(String expected, String received) {
        if (received == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8));
    }
}
