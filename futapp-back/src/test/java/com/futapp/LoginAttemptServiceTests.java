package com.futapp;

import com.futapp.exception.TooManyLoginAttemptsException;
import com.futapp.service.LoginAttemptService;
import com.futapp.model.Usuario;
import com.futapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoginAttemptServiceTests {
    @Autowired LoginAttemptService service;
    @Autowired UsuarioRepository usuarios;

    @Test
    void bloqueaDespuesDeCincoFallosYLimpiaConExito() {
        String email = "bloqueo-" + System.nanoTime() + "@test.local";
        Usuario usuario = new Usuario(); usuario.setNombre("Bloqueo"); usuario.setEmail(email);
        usuario.setPasswordHash("hash"); usuario.setRol("JUGADOR"); usuarios.saveAndFlush(usuario);

        for (int i = 0; i < 5; i++) { service.verify(email); service.failed(email); }
        assertThrows(TooManyLoginAttemptsException.class, () -> service.verify(email));
        Usuario bloqueado = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        assertNotNull(bloqueado.getLockedUntil());

        bloqueado.setLockedUntil(LocalDateTime.now().minusSeconds(1));
        usuarios.saveAndFlush(bloqueado);
        assertDoesNotThrow(() -> service.verify(email));
        assertEquals(0, usuarios.findByEmailIgnoreCase(email).orElseThrow().getFailedLoginAttempts());
    }
}
