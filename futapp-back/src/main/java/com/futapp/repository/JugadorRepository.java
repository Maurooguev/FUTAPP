package com.futapp.repository;

import com.futapp.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByEquipoIdEquipo(Long idEquipo);

    boolean existsByEquipoIdEquipoAndNumeroCamiseta(Long idEquipo, Integer numeroCamiseta);

    boolean existsByEquipoIdEquipoAndNumeroCamisetaAndIdJugadorNot(Long idEquipo, Integer numeroCamiseta, Long idJugador);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdJugadorNot(String dni, Long idJugador);
    Optional<Jugador> findByUsuarioIdUsuario(Long idUsuario);
    Optional<Jugador> findByCodigoPublicoIgnoreCase(String codigoPublico);
    Page<Jugador> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrApodoContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(
            String nombre, String apellido, String apodo, String codigoPublico, Pageable pageable);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Jugador j set j.visitas = j.visitas + 1 where j.idJugador = :id")
    int incrementarVisitas(@Param("id") Long id);
}
