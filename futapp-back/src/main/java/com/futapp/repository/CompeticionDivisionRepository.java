package com.futapp.repository;

import com.futapp.model.CompeticionDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompeticionDivisionRepository extends JpaRepository<CompeticionDivision, Long> {
    List<CompeticionDivision> findByTorneoIdTorneoOrderByOrden(Long idTorneo);
}
