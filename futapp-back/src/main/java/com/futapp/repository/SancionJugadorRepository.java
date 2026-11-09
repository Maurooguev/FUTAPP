package com.futapp.repository;

import com.futapp.model.SancionJugador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SancionJugadorRepository extends JpaRepository<SancionJugador, Long> {
    Optional<SancionJugador> findByTarjetaIdTarjeta(Long idTarjeta);
    void deleteByTarjetaIdTarjeta(Long idTarjeta);

    @EntityGraph(attributePaths = {"tarjeta", "tarjeta.jugador", "tarjeta.equipo", "tarjeta.partido", "tarjeta.partido.fecha", "tarjeta.partido.torneo"})
    List<SancionJugador> findByTarjetaPartidoTorneoIdTorneoOrderByCreadaEnDesc(Long idTorneo);
    List<SancionJugador> findByTarjetaPartidoTorneoIdTorneoAndTarjetaJugadorIdJugadorAndTarjetaEquipoIdEquipo(
            Long idTorneo, Long idJugador, Long idEquipo);
}

