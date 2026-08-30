package com.futapp.repository;

import com.futapp.model.CompeticionZona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompeticionZonaRepository extends JpaRepository<CompeticionZona, Long> {
    List<CompeticionZona> findByFaseIdFaseOrderByOrden(Long idFase);
}
