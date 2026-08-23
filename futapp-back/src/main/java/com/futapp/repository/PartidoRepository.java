package com.futapp.repository;

import com.futapp.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    List<Partido> findByTorneoIdTorneo(Long idTorneo);

    List<Partido> findByFechaIdFecha(Long idFecha);

    boolean existsByFechaIdFecha(Long idFecha);

    boolean existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(Long idEquipoLocal, Long idEquipoVisitante);

    boolean existsByTorneoIdTorneoAndEquipoLocalIdEquipoOrTorneoIdTorneoAndEquipoVisitanteIdEquipo(
            Long idTorneoLocal, Long idEquipoLocal, Long idTorneoVisitante, Long idEquipoVisitante);
}
