package com.futapp.repository;

import com.futapp.model.PerfilOrganizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilOrganizadorRepository extends JpaRepository<PerfilOrganizador, Long> {
    Optional<PerfilOrganizador> findByUsuarioIdUsuario(Long idUsuario);
    boolean existsByUsuarioIdUsuario(Long idUsuario);
}
