package com.futapp.service;

import com.futapp.exception.TooManyLoginAttemptsException;
import com.futapp.model.Usuario;
import com.futapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class LoginAttemptService {
    private final UsuarioRepository usuarios;
    private final int maximumFailures;
    private final Duration lockDuration;

    public LoginAttemptService(UsuarioRepository usuarios,
            @Value("${futapp.auth.login.maximum-failures:5}") int maximumFailures,
            @Value("${futapp.auth.login.lock-duration:15m}") Duration lockDuration) {
        if (maximumFailures < 1 || lockDuration.isZero() || lockDuration.isNegative()) {
            throw new IllegalArgumentException("La política de bloqueo de acceso es inválida");
        }
        this.usuarios = usuarios;
        this.maximumFailures = maximumFailures;
        this.lockDuration = lockDuration;
    }

    @Transactional
    public void verify(String email) {
        usuarios.findForUpdateByEmailIgnoreCase(normalize(email)).ifPresent(usuario -> {
            LocalDateTime now = LocalDateTime.now();
            if (usuario.getLockedUntil() != null && usuario.getLockedUntil().isAfter(now)) throw new TooManyLoginAttemptsException();
            if (usuario.getLockedUntil() != null || expired(usuario, now)) reset(usuario);
        });
    }

    @Transactional
    public void failed(String email) {
        usuarios.findForUpdateByEmailIgnoreCase(normalize(email)).ifPresent(usuario -> {
            LocalDateTime now = LocalDateTime.now();
            if (expired(usuario, now)) reset(usuario);
            if (usuario.getLoginFailureStartedAt() == null) usuario.setLoginFailureStartedAt(now);
            int failures = usuario.getFailedLoginAttempts() + 1;
            usuario.setFailedLoginAttempts(failures);
            if (failures >= maximumFailures) usuario.setLockedUntil(now.plus(lockDuration));
        });
    }

    @Transactional
    public void succeeded(String email) {
        usuarios.findForUpdateByEmailIgnoreCase(normalize(email)).ifPresent(this::reset);
    }

    private boolean expired(Usuario usuario, LocalDateTime now) {
        return usuario.getLoginFailureStartedAt() != null && usuario.getLoginFailureStartedAt().plus(lockDuration).isBefore(now);
    }
    private void reset(Usuario usuario) {
        usuario.setFailedLoginAttempts(0); usuario.setLoginFailureStartedAt(null); usuario.setLockedUntil(null);
    }
    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
