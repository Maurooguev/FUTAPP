package com.futapp.repository;

import com.futapp.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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
    Page<Torneo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(
            String nombre, String codigoPublico, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Torneo t where t.idTorneo = :id")
    Optional<Torneo> findByIdForUpdate(@Param("id") Long id);
}
