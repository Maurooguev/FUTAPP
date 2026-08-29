package com.futapp.repository;

import com.futapp.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    List<Tarjeta> findByPartidoIdPartido(Long idPartido);

    List<Tarjeta> findByJugadorIdJugador(Long idJugador);
    List<Tarjeta> findByEquipoIdEquipo(Long idEquipo);

    boolean existsByPartidoIdPartido(Long idPartido);

    boolean existsByJugadorIdJugador(Long idJugador);
    boolean existsByPartidoTorneoIdTorneo(Long idTorneo);
}
