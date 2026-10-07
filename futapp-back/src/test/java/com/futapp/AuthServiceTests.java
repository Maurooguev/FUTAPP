package com.futapp;

import com.futapp.dto.LoginRequestDTO;
import com.futapp.dto.RegisterRequestDTO;
import com.futapp.dto.ForgotPasswordRequestDTO;
import com.futapp.dto.ResetPasswordRequestDTO;
import com.futapp.service.AuthService;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.PasswordResetTokenRepository;
import com.futapp.security.TokenSecurity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTests {
    @Autowired private AuthService authService;
    @Autowired private AuthTokenRepository authTokens;
    @Autowired private PasswordResetTokenRepository resetTokens;

    @Test
    void registraEIniciaSesionConCredencialesReales() {
        var registration = authService.register(new RegisterRequestDTO("Organizador Test", "organizador@test.local", "password123", "ORGANIZADOR"));
        assertNotNull(registration.token());
        assertEquals("ORGANIZADOR", registration.rol());
        assertTrue(registration.roles().contains("ORGANIZADOR"));
        assertEquals(java.util.Set.of("ORGANIZADOR"), registration.roles());

        var login = authService.login(new LoginRequestDTO("organizador@test.local", "password123"));
        assertNotNull(login.token());
        assertEquals(registration.email(), login.email());
    }

    @Test
    void recuperaPasswordConTokenTemporal() {
        authService.register(new RegisterRequestDTO("Recuperación Test", "recuperar@test.local", "password123", "JUGADOR"));
        authService.login(new LoginRequestDTO("recuperar@test.local", "password123"));
        var request = authService.forgotPassword(new ForgotPasswordRequestDTO("recuperar@test.local"));
        assertNotNull(request.tokenDesarrollo());

        authService.resetPassword(new ResetPasswordRequestDTO(request.tokenDesarrollo(), "password456"));
        assertTrue(authTokens.findAll().isEmpty(), "El cambio debe revocar todas las sesiones anteriores");

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequestDTO("recuperar@test.local", "password123")));
        assertNotNull(authService.login(new LoginRequestDTO("recuperar@test.local", "password456")).token());
        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordRequestDTO(request.tokenDesarrollo(), "password789")));
    }

    @Test
    void rechazaTokenVencidoYContrasenaRepetida() {
        authService.register(new RegisterRequestDTO("Vencimiento Test", "vence@test.local", "password123", "JUGADOR"));
        var solicitud = authService.forgotPassword(new ForgotPasswordRequestDTO("vence@test.local"));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(
                new ResetPasswordRequestDTO(solicitud.tokenDesarrollo(), "password123")));

        var token = resetTokens.findById(TokenSecurity.hash(solicitud.tokenDesarrollo())).orElseThrow();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        resetTokens.saveAndFlush(token);
        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(
                new ResetPasswordRequestDTO(solicitud.tokenDesarrollo(), "password456")));
        assertNotNull(authService.login(new LoginRequestDTO("vence@test.local", "password123")).token());
    }

    @Test
    void noRevelaSiElEmailExiste() {
        authService.register(new RegisterRequestDTO("Email Test", "existe@test.local", "password123", "JUGADOR"));
        var existente = authService.forgotPassword(new ForgotPasswordRequestDTO("existe@test.local"));
        var inexistente = authService.forgotPassword(new ForgotPasswordRequestDTO("otro@test.local"));
        assertEquals(existente.mensaje(), inexistente.mensaje());
        assertNotNull(existente.tokenDesarrollo()); // Solo se expone en el perfil de pruebas.
        assertNull(inexistente.tokenDesarrollo());
    }

    @Test
    void conservaSoloLasCincoSesionesMasRecientes() {
        var registration = authService.register(new RegisterRequestDTO(
                "Sesiones Test", "sesiones@test.local", "password123", "JUGADOR"));
        for (int i = 0; i < 7; i++) {
            authService.login(new LoginRequestDTO("sesiones@test.local", "password123"));
        }
        assertEquals(5, authTokens.findByUsuarioIdUsuarioOrderByExpiresAtDesc(registration.idUsuario()).size());
    }
}
