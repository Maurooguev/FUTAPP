package com.futapp.repository;

import com.futapp.model.SolicitudReclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudReclamoRepository extends JpaRepository<SolicitudReclamo, Long> {
    List<SolicitudReclamo> findByEstadoOrderByFechaSolicitudAsc(String estado);
    List<SolicitudReclamo> findBySolicitanteIdUsuarioOrderByFechaSolicitudDesc(Long idUsuario);
    List<SolicitudReclamo> findAllByOrderByFechaSolicitudDesc();
    boolean existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEstado(Long idUsuario, Long idEquipo, String estado);
    boolean existsBySolicitanteIdUsuarioAndJugadorIdJugadorAndEstado(Long idUsuario, Long idJugador, String estado);
}
