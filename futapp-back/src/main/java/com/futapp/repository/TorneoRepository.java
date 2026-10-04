package com.futapp.repository;

import com.futapp.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByCodigoPublico(String codigoPublico);
    Optional<Torneo> findByCodigoPublicoIgnoreCase(String codigoPublico);
    List<Torneo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(String nombre, String codigoPublico);
    List<Torneo> findByPropietarioIdUsuario(Long idUsuario);
    List<Torneo> findByCampeonIdEquipoOrderByIdTorneoDesc(Long idEquipo);
    Page<Torneo> findByEstadoNotIgnoreCase(String estado, Pageable pageable);
    List<Torneo> findByEstadoIn(Collection<String> estados);
    Page<Torneo> findByEstadoIn(Collection<String> estados, Pageable pageable);
    @Query("select t from Torneo t where t.estado in :estados and "
            + "(lower(t.nombre) like lower(concat('%', :termino, '%')) or "
            + "lower(t.codigoPublico) like lower(concat('%', :termino, '%')))")
    Page<Torneo> buscarPublicos(@Param("termino") String termino,
                                @Param("estados") Collection<String> estados, Pageable pageable);
    @Query("select t from Torneo t where t.estado in :estados and "
            + "(lower(t.nombre) like lower(concat('%', :termino, '%')) or "
            + "lower(t.codigoPublico) like lower(concat('%', :termino, '%')) or "
            + "lower(t.ubicacion) like lower(concat('%', :termino, '%')))")
    Page<Torneo> buscarPublicosGlobal(@Param("termino") String termino,
                                      @Param("estados") Collection<String> estados, Pageable pageable);
    Page<Torneo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(
            String nombre, String codigoPublico, Pageable pageable);
    Page<Torneo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCaseOrUbicacionContainingIgnoreCase(
            String nombre, String codigoPublico, String ubicacion, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Torneo t where t.idTorneo = :id")
    Optional<Torneo> findByIdForUpdate(@Param("id") Long id);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Torneo t set t.visitas = t.visitas + 1 where t.idTorneo = :id")
    int incrementarVisitas(@Param("id") Long id);
}
