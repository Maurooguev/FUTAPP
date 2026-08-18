package com.futapp.repository;

import com.futapp.model.InscripcionEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscripcionEquipoRepository extends JpaRepository<InscripcionEquipo, Long> {
    List<InscripcionEquipo> findByEquipoIdEquipoOrderByFechaAltaDesc(Long idEquipo);
    List<InscripcionEquipo> findByTorneoIdTorneo(Long idTorneo);
    Optional<InscripcionEquipo> findByEquipoIdEquipoAndTorneoIdTorneo(Long idEquipo, Long idTorneo);
    void deleteByEquipoIdEquipo(Long idEquipo);
}
