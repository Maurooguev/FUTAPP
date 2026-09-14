package com.futapp.repository;

import com.futapp.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    void deleteByUsuarioIdUsuario(Long idUsuario);
    long deleteByExpiresAtBefore(java.time.LocalDateTime fecha);
    long deleteByUsedAtIsNotNullAndUsedAtBefore(java.time.LocalDateTime fecha);
}
