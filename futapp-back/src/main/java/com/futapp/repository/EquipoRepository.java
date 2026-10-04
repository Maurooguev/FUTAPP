package com.futapp.repository;

import com.futapp.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoIdTorneo(Long idTorneo);
    List<Equipo> findByCreadorIdUsuarioOrderByNombre(Long idUsuario);
    Optional<Equipo> findByCodigoPublicoIgnoreCase(String codigoPublico);
    Page<Equipo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCaseOrBarrioContainingIgnoreCase(
            String nombre, String codigoPublico, String barrio, Pageable pageable);
    Page<Equipo> findByTorneoEstadoIn(List<String> estados, Pageable pageable);
    @Query("select e from Equipo e where e.torneo.estado in :estados and (lower(e.nombre) like lower(concat('%', :q, '%')) or lower(e.codigoPublico) like lower(concat('%', :q, '%')) or lower(e.barrio) like lower(concat('%', :q, '%')))")
    Page<Equipo> buscarPublicos(@Param("q") String query, @Param("estados") List<String> estados, Pageable pageable);
}
