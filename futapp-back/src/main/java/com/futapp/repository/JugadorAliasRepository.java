package com.futapp.repository;

import com.futapp.model.JugadorAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JugadorAliasRepository extends JpaRepository<JugadorAlias, Long> {
    Optional<JugadorAlias> findByCodigoIgnoreCase(String codigo);
}
