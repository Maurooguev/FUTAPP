package com.futapp.repository;

import com.futapp.model.Fecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FechaRepository extends JpaRepository<Fecha, Long> {

    List<Fecha> findByTorneoIdTorneo(Long idTorneo);

    boolean existsByTorneoIdTorneoAndNumeroFecha(Long idTorneo, Integer numeroFecha);

    boolean existsByTorneoIdTorneoAndNumeroFechaAndIdFechaNot(Long idTorneo, Integer numeroFecha, Long idFecha);
    Optional<Fecha> findByTorneoIdTorneoAndNumeroFecha(Long idTorneo, Integer numeroFecha);
}
