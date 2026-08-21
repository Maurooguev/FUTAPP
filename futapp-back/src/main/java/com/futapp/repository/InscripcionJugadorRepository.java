package com.futapp.repository;

import com.futapp.model.InscripcionJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscripcionJugadorRepository extends JpaRepository<InscripcionJugador, Long> {
    List<InscripcionJugador> findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(Long idInscripcion);
    List<InscripcionJugador> findByJugadorIdJugadorOrderByFechaAltaDesc(Long idJugador);
    long countByInscripcionEquipoIdInscripcion(Long idInscripcion);
    long countByInscripcionEquipoIdInscripcionAndEstado(Long idInscripcion, String estado);
    boolean existsByInscripcionEquipoIdInscripcionAndJugadorIdJugador(Long idInscripcion, Long idJugador);
    Optional<InscripcionJugador> findByInscripcionEquipoIdInscripcionAndJugadorIdJugador(
            Long idInscripcion, Long idJugador);
    boolean existsByInscripcionEquipoIdInscripcionAndNumeroCamiseta(Long idInscripcion, Integer numeroCamiseta);
    boolean existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstadoAndIdInscripcionJugadorNot(
            Long idInscripcion, Integer numeroCamiseta, String estado, Long idInscripcionJugador);
    boolean existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstado(
            Long idInscripcion, Integer numeroCamiseta, String estado);
    void deleteByJugadorIdJugador(Long idJugador);
    void deleteByInscripcionEquipoEquipoIdEquipo(Long idEquipo);
}
