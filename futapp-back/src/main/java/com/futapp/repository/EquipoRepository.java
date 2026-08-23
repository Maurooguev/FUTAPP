package com.futapp.repository;

import com.futapp.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoIdTorneo(Long idTorneo);
    List<Equipo> findByCreadorIdUsuarioOrderByNombre(Long idUsuario);
    Optional<Equipo> findByCodigoPublicoIgnoreCase(String codigoPublico);
}
