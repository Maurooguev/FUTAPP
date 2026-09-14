package com.futapp.repository;

import com.futapp.model.AuditoriaAdministrativa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAdministrativaRepository extends JpaRepository<AuditoriaAdministrativa, Long> {
    Page<AuditoriaAdministrativa> findAllByOrderByFechaDesc(Pageable pageable);
}
