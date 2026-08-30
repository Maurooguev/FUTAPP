package com.futapp.service;

import com.futapp.dto.*;
import com.futapp.model.AuthToken;
import com.futapp.model.PasswordResetToken;
import com.futapp.model.Usuario;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.PasswordResetTokenRepository;
import com.futapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private final UsuarioRepository usuarios;
    private final AuthTokenRepository tokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder encoder;
    private final boolean exposeResetToken;
    public AuthService(UsuarioRepository usuarios, AuthTokenRepository tokens,
                       PasswordResetTokenRepository resetTokens, PasswordEncoder encoder,
                       @Value("${futapp.password-reset.expose-token:false}") boolean exposeResetToken) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.resetTokens = resetTokens;
        this.encoder = encoder;
        this.exposeResetToken = exposeResetToken;
    }
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        String rol = request.rol() == null || request.rol().isBlank() ? "ORGANIZADOR" : request.rol().trim().toUpperCase();
        if (!java.util.Set.of("ORGANIZADOR", "DIRECTOR_EQUIPO", "JUGADOR").contains(rol)) {
            throw new IllegalArgumentException("Tipo de cuenta inválido");
        }
        Usuario user = new Usuario(); user.setNombre(request.nombre().trim()); user.setEmail(email); user.setPasswordHash(encoder.encode(request.password())); user.setRol(rol);
        if (!"ADMIN".equals(rol)) user.agregarRol("JUGADOR");
        return createToken(usuarios.save(user));
    }
    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario user = usuarios.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw new IllegalArgumentException("Email o contraseña incorrectos");
        return createToken(user);
    }
    public PasswordResetResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        String rawToken = usuarios.findByEmailIgnoreCase(request.email().trim()).map(user -> {
            resetTokens.deleteByUsuarioIdUsuario(user.getIdUsuario());
            String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken reset = new PasswordResetToken();
            reset.setTokenHash(hash(token));
            reset.setUsuario(user);
            reset.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            resetTokens.save(reset);
            return token;
        }).orElse(null);
        return new PasswordResetResponseDTO(
                "Si el email está registrado, generamos un enlace válido durante 30 minutos.",
                exposeResetToken ? rawToken : null);
    }
    public PasswordResetResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken reset = resetTokens.findByTokenHashAndUsedAtIsNull(hash(request.token()))
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
    public void logout(String token) { tokens.deleteById(token); }
    private AuthResponseDTO createToken(Usuario user) {
        AuthToken authToken = new AuthToken(); authToken.setToken(UUID.randomUUID().toString().replace("-", "")); authToken.setUsuario(user); authToken.setExpiresAt(LocalDateTime.now().plusDays(7)); tokens.save(authToken);
        return new AuthResponseDTO(authToken.getToken(), user.getIdUsuario(), user.getNombre(), user.getEmail(), user.getRol(), user.getRoles());
    }
    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo proteger el token", exception);
        }
    }
}
