package com.futapp.repository;

import com.futapp.model.CompeticionFase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompeticionFaseRepository extends JpaRepository<CompeticionFase, Long> {
    List<CompeticionFase> findByDivisionIdDivisionOrderByOrden(Long idDivision);
}
