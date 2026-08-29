package com.futapp.repository;

import com.futapp.model.EquipoAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoAliasRepository extends JpaRepository<EquipoAlias, Long> {
    Optional<EquipoAlias> findByCodigoIgnoreCase(String codigo);
    List<EquipoAlias> findByEquipoIdEquipo(Long idEquipo);
}
