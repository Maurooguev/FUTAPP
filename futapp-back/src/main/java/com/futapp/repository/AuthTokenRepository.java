package com.futapp.repository;
import com.futapp.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    void deleteByUsuarioIdUsuario(Long idUsuario);
    long deleteByExpiresAtBefore(java.time.LocalDateTime fecha);
    List<AuthToken> findByUsuarioIdUsuarioOrderByExpiresAtDesc(Long idUsuario);
}
