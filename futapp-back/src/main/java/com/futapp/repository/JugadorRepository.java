package com.futapp.repository;

import com.futapp.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByEquipoIdEquipo(Long idEquipo);

    boolean existsByEquipoIdEquipoAndNumeroCamiseta(Long idEquipo, Integer numeroCamiseta);

    boolean existsByEquipoIdEquipoAndNumeroCamisetaAndIdJugadorNot(Long idEquipo, Integer numeroCamiseta, Long idJugador);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdJugadorNot(String dni, Long idJugador);
    Optional<Jugador> findByUsuarioIdUsuario(Long idUsuario);
}
