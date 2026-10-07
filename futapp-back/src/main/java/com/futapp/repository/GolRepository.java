package com.futapp.repository;

import com.futapp.dto.GoleadorDTO;
import com.futapp.dto.GolResponseDTO;
import com.futapp.model.Gol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GolRepository extends JpaRepository<Gol, Long> {

    List<Gol> findByPartidoIdPartido(Long idPartido);

    List<Gol> findByJugadorIdJugador(Long idJugador);
    List<Gol> findByEquipoIdEquipo(Long idEquipo);

    boolean existsByPartidoIdPartido(Long idPartido);

    boolean existsByJugadorIdJugador(Long idJugador);
    long countByJugadorIdJugador(Long idJugador);
    boolean existsByPartidoTorneoIdTorneo(Long idTorneo);
    long countByPartidoIdPartidoAndEquipoIdEquipo(Long idPartido, Long idEquipo);

    @Query("""
        SELECT new com.futapp.dto.GolResponseDTO(
            g.idGol, g.minuto, g.tipoGol, g.observacion,
            p.idPartido, j.idJugador, CONCAT(j.nombre, ' ', j.apellido),
            e.idEquipo, e.nombre
        )
        FROM Gol g
        JOIN g.partido p
        JOIN g.jugador j
        JOIN g.equipo e
        WHERE p.idPartido = :idPartido
        ORDER BY g.minuto, g.idGol
    """)
    List<GolResponseDTO> obtenerDetallePorPartido(Long idPartido);

    @Query("""
        SELECT new com.futapp.dto.GolResponseDTO(
            g.idGol, g.minuto, g.tipoGol, g.observacion,
            p.idPartido, j.idJugador, CONCAT(j.nombre, ' ', j.apellido),
            e.idEquipo, e.nombre
        )
        FROM Gol g
        JOIN g.partido p
        JOIN g.jugador j
        JOIN g.equipo e
        WHERE j.idJugador = :idJugador
        ORDER BY p.fechaHora DESC, g.minuto, g.idGol
    """)
    List<GolResponseDTO> obtenerDetallePorJugador(Long idJugador);

    @Query("""
        SELECT new com.futapp.dto.GoleadorDTO(
            j.idJugador,
            CONCAT(j.nombre, ' ', j.apellido),
            j.foto,
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
        GROUP BY j.idJugador, j.nombre, j.apellido, j.foto, e.idEquipo, e.nombre
        ORDER BY COUNT(g.idGol) DESC
    """)
    List<GoleadorDTO> obtenerGoleadoresPorTorneo(Long idTorneo);
}
