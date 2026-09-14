package com.futapp.repository;

import com.futapp.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Partido p where p.idPartido = :id")
    Optional<Partido> findByIdForUpdate(@Param("id") Long id);

    List<Partido> findByTorneoIdTorneo(Long idTorneo);
    Page<Partido> findByTorneoIdTorneo(Long idTorneo, Pageable pageable);

    List<Partido> findByFechaIdFecha(Long idFecha);
    List<Partido> findByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(Long idEquipoLocal, Long idEquipoVisitante);

    boolean existsByFechaIdFecha(Long idFecha);

    boolean existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(Long idEquipoLocal, Long idEquipoVisitante);

    boolean existsByTorneoIdTorneoAndEquipoLocalIdEquipoOrTorneoIdTorneoAndEquipoVisitanteIdEquipo(
            Long idTorneoLocal, Long idEquipoLocal, Long idTorneoVisitante, Long idEquipoVisitante);
}
