package com.futapp.repository;

import com.futapp.model.SolicitudReclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

public interface SolicitudReclamoRepository extends JpaRepository<SolicitudReclamo, Long> {
    @EntityGraph(attributePaths = {"solicitante", "torneo", "torneo.propietario", "equipo", "equipoDestino", "jugador", "jugador.equipo", "revisor"})
    List<SolicitudReclamo> findByEstadoOrderByFechaSolicitudAsc(String estado);
    @EntityGraph(attributePaths = {"solicitante", "torneo", "torneo.propietario", "equipo", "equipoDestino", "jugador", "jugador.equipo", "revisor"})
    List<SolicitudReclamo> findBySolicitanteIdUsuarioOrderByFechaSolicitudDesc(Long idUsuario);
    @EntityGraph(attributePaths = {"solicitante", "torneo", "torneo.propietario", "equipo", "equipoDestino", "jugador", "jugador.equipo", "revisor"})
    List<SolicitudReclamo> findAllByOrderByFechaSolicitudDesc();
    boolean existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEstado(Long idUsuario, Long idEquipo, String estado);
    boolean existsBySolicitanteIdUsuarioAndJugadorIdJugadorAndEstado(Long idUsuario, Long idJugador, String estado);
    boolean existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEquipoDestinoIdEquipoAndEstado(Long userId, Long teamId, Long targetId, String estado);
    List<SolicitudReclamo> findByEquipoIdEquipo(Long idEquipo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"solicitante", "torneo", "torneo.propietario", "equipo", "equipoDestino", "jugador", "jugador.equipo", "revisor"})
    @Query("select r from SolicitudReclamo r where r.idSolicitud = :id")
    Optional<SolicitudReclamo> findByIdForUpdate(@Param("id") Long id);
}
