package com.futapp.repository;

import com.futapp.dto.GoleadorDTO;
import com.futapp.model.Gol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GolRepository extends JpaRepository<Gol, Long> {

    List<Gol> findByPartidoIdPartido(Long idPartido);

    List<Gol> findByJugadorIdJugador(Long idJugador);

    boolean existsByPartidoIdPartido(Long idPartido);

    boolean existsByJugadorIdJugador(Long idJugador);
    boolean existsByPartidoTorneoIdTorneo(Long idTorneo);

    @Query("""
        SELECT new com.futapp.dto.GoleadorDTO(
            j.idJugador,
            CONCAT(j.nombre, ' ', j.apellido),
            e.idEquipo,
            e.nombre,
            COUNT(g.idGol)
        )
        FROM Gol g
        JOIN g.jugador j
        JOIN g.equipo e
        JOIN g.partido p
        JOIN p.torneo t
        WHERE t.idTorneo = :idTorneo
        GROUP BY j.idJugador, j.nombre, j.apellido, e.idEquipo, e.nombre
        ORDER BY COUNT(g.idGol) DESC
    """)
    List<GoleadorDTO> obtenerGoleadoresPorTorneo(Long idTorneo);
}
