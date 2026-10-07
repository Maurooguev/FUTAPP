package com.futapp.repository;

import com.futapp.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    @Override
    @EntityGraph(attributePaths = {"partido", "jugador", "equipo"})
    List<Tarjeta> findAll();

    @EntityGraph(attributePaths = {"partido", "jugador", "equipo"})
    List<Tarjeta> findByPartidoIdPartido(Long idPartido);

    @EntityGraph(attributePaths = {"partido", "jugador", "equipo"})
    List<Tarjeta> findByJugadorIdJugador(Long idJugador);
    @EntityGraph(attributePaths = {"partido", "jugador", "equipo"})
    List<Tarjeta> findByPartidoTorneoIdTorneo(Long idTorneo);
    List<Tarjeta> findByEquipoIdEquipo(Long idEquipo);

    boolean existsByPartidoIdPartido(Long idPartido);

    boolean existsByJugadorIdJugador(Long idJugador);
    long countByJugadorIdJugadorAndTipoTarjetaIgnoreCase(Long idJugador, String tipoTarjeta);
    boolean existsByPartidoTorneoIdTorneo(Long idTorneo);
}
