package com.futapp.controller;
import com.futapp.dto.*;
import com.futapp.model.Usuario;
import com.futapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) { return auth.register(request); }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) { return auth.login(request); }

    @GetMapping("/me")
    public UsuarioSesionDTO me(@AuthenticationPrincipal Usuario usuario) { return UsuarioSesionDTO.from(usuario); }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String header) {
        auth.logout(header.substring("Bearer ".length()));
    }
}
