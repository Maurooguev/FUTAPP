package com.futapp.service;

import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class TokenMaintenanceService {
    private final AuthTokenRepository authTokens;
    private final PasswordResetTokenRepository resetTokens;

    public TokenMaintenanceService(AuthTokenRepository authTokens, PasswordResetTokenRepository resetTokens) {
        this.authTokens = authTokens;
        this.resetTokens = resetTokens;
    }

    @Scheduled(cron = "${futapp.maintenance.token-cleanup-cron:0 15 3 * * *}")
    @Transactional
    public void limpiarTokensVencidos() {
        LocalDateTime ahora = LocalDateTime.now();
        authTokens.deleteByExpiresAtBefore(ahora);
        resetTokens.deleteByExpiresAtBefore(ahora);
        resetTokens.deleteByUsedAtIsNotNullAndUsedAtBefore(ahora.minusDays(1));
    }
}
