package com.futapp.repository;

import com.futapp.model.CupoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CupoEquipoRepository extends JpaRepository<CupoEquipo, Long> {
    List<CupoEquipo> findByDivisionIdDivisionOrderByNumero(Long idDivision);
    List<CupoEquipo> findByDivisionTorneoIdTorneoOrderByNumero(Long idTorneo);
}
