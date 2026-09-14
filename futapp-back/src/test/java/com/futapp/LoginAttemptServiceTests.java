package com.futapp;

import com.futapp.exception.TooManyLoginAttemptsException;
import com.futapp.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTests {
    @Test
    void bloqueaDespuesDeCincoFallosYLimpiaConExito() {
        LoginAttemptService service = new LoginAttemptService();
        String key = "127.0.0.1:usuario@test.local";
        for (int i = 0; i < 5; i++) { service.verify(key); service.failed(key); }
        assertThrows(TooManyLoginAttemptsException.class, () -> service.verify(key));
        service.succeeded(key);
        assertDoesNotThrow(() -> service.verify(key));
    }
}
