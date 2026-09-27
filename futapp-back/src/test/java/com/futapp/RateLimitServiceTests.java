package com.futapp;

import com.futapp.service.RateLimitService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTests {
    @Test
    void bloqueaAlSuperarElLimiteSinIncrementarDuranteElBloqueo() {
        RateLimitService service = new RateLimitService();
        var primera = service.consume("registro:cliente", 2, Duration.ofMinutes(1));
        var segunda = service.consume("registro:cliente", 2, Duration.ofMinutes(1));
        var bloqueada = service.consume("registro:cliente", 2, Duration.ofMinutes(1));

        assertTrue(primera.allowed());
        assertEquals(1, primera.remaining());
        assertTrue(segunda.allowed());
        assertEquals(0, segunda.remaining());
        assertFalse(bloqueada.allowed());
        assertEquals(0, bloqueada.remaining());
        assertTrue(bloqueada.retryAfterSeconds() > 0);
    }
}
