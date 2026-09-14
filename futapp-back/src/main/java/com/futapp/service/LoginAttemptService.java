package com.futapp.service;

import com.futapp.exception.TooManyLoginAttemptsException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private record Attempt(int failures, Instant firstFailure) {}
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void verify(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) return;
        if (attempt.firstFailure().isBefore(Instant.now().minus(15, ChronoUnit.MINUTES))) { attempts.remove(key); return; }
        if (attempt.failures() >= 5) throw new TooManyLoginAttemptsException();
    }
    public void failed(String key) { attempts.compute(key, (ignored, old) -> old == null || old.firstFailure().isBefore(Instant.now().minus(15, ChronoUnit.MINUTES)) ? new Attempt(1, Instant.now()) : new Attempt(old.failures() + 1, old.firstFailure())); }
    public void succeeded(String key) { attempts.remove(key); }
}
