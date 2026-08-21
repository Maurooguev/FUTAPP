package com.futapp.repository;
import com.futapp.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findTop100ByUsuarioIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);
    long countByUsuarioIdUsuarioAndFechaLecturaIsNull(Long idUsuario);
}
