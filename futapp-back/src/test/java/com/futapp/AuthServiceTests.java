package com.futapp;

import com.futapp.dto.LoginRequestDTO;
import com.futapp.dto.RegisterRequestDTO;
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
        var registration = authService.register(new RegisterRequestDTO("Organizador Test", "organizador@test.local", "password123"));
        assertNotNull(registration.token());
        assertEquals("ORGANIZADOR", registration.rol());

        var login = authService.login(new LoginRequestDTO("organizador@test.local", "password123"));
        assertNotNull(login.token());
        assertEquals(registration.email(), login.email());
    }
}
