package com.futapp.repository;
import com.futapp.model.ResultadoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ResultadoAuditoriaRepository extends JpaRepository<ResultadoAuditoria, Long> {
    List<ResultadoAuditoria> findByPartidoIdPartidoOrderByFechaCambioDesc(Long idPartido);
}
