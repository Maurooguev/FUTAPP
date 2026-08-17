package com.futapp.service;

import com.futapp.dto.*;
import com.futapp.model.AuthToken;
import com.futapp.model.Usuario;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final AuthTokenRepository tokens;
    private final PasswordEncoder encoder;
    public AuthService(UsuarioRepository usuarios, AuthTokenRepository tokens, PasswordEncoder encoder) { this.usuarios = usuarios; this.tokens = tokens; this.encoder = encoder; }
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        Usuario user = new Usuario(); user.setNombre(request.nombre().trim()); user.setEmail(email); user.setPasswordHash(encoder.encode(request.password())); user.setRol("ORGANIZADOR");
        return createToken(usuarios.save(user));
    }
    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario user = usuarios.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw new IllegalArgumentException("Email o contraseña incorrectos");
        return createToken(user);
    }
    public void logout(String token) { tokens.deleteById(token); }
    private AuthResponseDTO createToken(Usuario user) {
        AuthToken authToken = new AuthToken(); authToken.setToken(UUID.randomUUID().toString().replace("-", "")); authToken.setUsuario(user); authToken.setExpiresAt(LocalDateTime.now().plusDays(7)); tokens.save(authToken);
        return new AuthResponseDTO(authToken.getToken(), user.getIdUsuario(), user.getNombre(), user.getEmail(), user.getRol());
    }
}
