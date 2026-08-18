package com.futapp.repository;

import com.futapp.model.InscripcionJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscripcionJugadorRepository extends JpaRepository<InscripcionJugador, Long> {
    List<InscripcionJugador> findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(Long idInscripcion);
    List<InscripcionJugador> findByJugadorIdJugadorOrderByFechaAltaDesc(Long idJugador);
    long countByInscripcionEquipoIdInscripcion(Long idInscripcion);
    boolean existsByInscripcionEquipoIdInscripcionAndJugadorIdJugador(Long idInscripcion, Long idJugador);
    Optional<InscripcionJugador> findByInscripcionEquipoIdInscripcionAndJugadorIdJugador(
            Long idInscripcion, Long idJugador);
    void deleteByJugadorIdJugador(Long idJugador);
    void deleteByInscripcionEquipoEquipoIdEquipo(Long idEquipo);
}
