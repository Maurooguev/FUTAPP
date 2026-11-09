package com.futapp.repository;

import com.futapp.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    @Override
    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    Optional<Partido> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Partido p where p.idPartido = :id")
    Optional<Partido> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    List<Partido> findByTorneoIdTorneo(Long idTorneo);
    Page<Partido> findByTorneoIdTorneo(Long idTorneo, Pageable pageable);

    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    Page<Partido> findByEstadoIgnoreCaseOrderByFechaHoraDesc(String estado, Pageable pageable);

    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    Page<Partido> findByEstadoIgnoreCaseAndTorneoEstadoInOrderByFechaHoraDesc(
            String estado, List<String> estadosTorneo, Pageable pageable);

    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    Page<Partido> findByEstadoIgnoreCaseAndFechaHoraGreaterThanEqualOrderByFechaHoraAsc(
            String estado, LocalDateTime desde, Pageable pageable);

    @EntityGraph(attributePaths = {"torneo", "fecha", "equipoLocal", "equipoVisitante", "mvp"})
    Page<Partido> findByEstadoIgnoreCaseAndFechaHoraGreaterThanEqualAndTorneoEstadoInOrderByFechaHoraAsc(
            String estado, LocalDateTime desde, List<String> estadosTorneo, Pageable pageable);

    List<Partido> findByFechaIdFecha(Long idFecha);
    List<Partido> findByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(Long idEquipoLocal, Long idEquipoVisitante);

    boolean existsByFechaIdFecha(Long idFecha);

    boolean existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(Long idEquipoLocal, Long idEquipoVisitante);

    boolean existsByTorneoIdTorneoAndEquipoLocalIdEquipoOrTorneoIdTorneoAndEquipoVisitanteIdEquipo(
            Long idTorneoLocal, Long idEquipoLocal, Long idTorneoVisitante, Long idEquipoVisitante);

    long countByMvpIdJugador(Long idJugador);

    @Query("""
        select count(distinct p.idPartido) from Partido p, InscripcionJugador ij
        where ij.jugador.idJugador = :idJugador
          and (lower(ij.posicion) like '%arquero%' or lower(ij.posicion) like '%defensor%')
          and p.torneo.idTorneo = ij.inscripcionEquipo.torneo.idTorneo
          and (p.estado = 'JUGADO' or p.estado = 'FINALIZADO')
          and ((p.equipoLocal.idEquipo = ij.inscripcionEquipo.equipo.idEquipo and p.golesVisitante = 0)
            or (p.equipoVisitante.idEquipo = ij.inscripcionEquipo.equipo.idEquipo and p.golesLocal = 0))
    """)
    long countVallasInvictasJugador(@Param("idJugador") Long idJugador);

    @Query("""
        select count(distinct p.idPartido) from Partido p, InscripcionJugador ij
        where ij.jugador.idJugador = :idJugador
          and lower(ij.posicion) like '%delantero%'
          and p.torneo.idTorneo = ij.inscripcionEquipo.torneo.idTorneo
          and (p.estado = 'JUGADO' or p.estado = 'FINALIZADO')
          and (p.equipoLocal.idEquipo = ij.inscripcionEquipo.equipo.idEquipo
            or p.equipoVisitante.idEquipo = ij.inscripcionEquipo.equipo.idEquipo)
    """)
    long countPartidosComputablesDelantero(@Param("idJugador") Long idJugador);

    @Query("""
        select count(distinct p.idPartido) from Partido p, InscripcionJugador ij
        where ij.jugador.idJugador = :idJugador
          and p.torneo.idTorneo = ij.inscripcionEquipo.torneo.idTorneo
          and (p.estado = 'JUGADO' or p.estado = 'FINALIZADO')
          and (p.equipoLocal.idEquipo = ij.inscripcionEquipo.equipo.idEquipo
            or p.equipoVisitante.idEquipo = ij.inscripcionEquipo.equipo.idEquipo)
    """)
    long countPartidosComputablesJugador(@Param("idJugador") Long idJugador);
}
