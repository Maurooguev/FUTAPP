package com.futapp;

import com.futapp.dto.LoginRequestDTO;
import com.futapp.dto.RegisterRequestDTO;
import com.futapp.dto.ForgotPasswordRequestDTO;
import com.futapp.dto.ResetPasswordRequestDTO;
import com.futapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTests {
    @Autowired private AuthService authService;

    @Test
    void registraEIniciaSesionConCredencialesReales() {
        var registration = authService.register(new RegisterRequestDTO("Organizador Test", "organizador@test.local", "password123", "ORGANIZADOR"));
        assertNotNull(registration.token());
        assertEquals("ORGANIZADOR", registration.rol());
        assertTrue(registration.roles().contains("ORGANIZADOR"));
        assertTrue(registration.roles().contains("JUGADOR"));

        var login = authService.login(new LoginRequestDTO("organizador@test.local", "password123"));
        assertNotNull(login.token());
        assertEquals(registration.email(), login.email());
    }

    @Test
    void recuperaPasswordConTokenTemporal() {
        authService.register(new RegisterRequestDTO("Recuperación Test", "recuperar@test.local", "password123", "JUGADOR"));
        var request = authService.forgotPassword(new ForgotPasswordRequestDTO("recuperar@test.local"));
        assertNotNull(request.tokenDesarrollo());

        authService.resetPassword(new ResetPasswordRequestDTO(request.tokenDesarrollo(), "password456"));

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequestDTO("recuperar@test.local", "password123")));
        assertNotNull(authService.login(new LoginRequestDTO("recuperar@test.local", "password456")).token());
        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordRequestDTO(request.tokenDesarrollo(), "password789")));
    }
}
