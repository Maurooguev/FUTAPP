package com.futapp.repository;

import com.futapp.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByCodigoPublico(String codigoPublico);
    Optional<Torneo> findByCodigoPublicoIgnoreCase(String codigoPublico);
    List<Torneo> findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(String nombre, String codigoPublico);
    List<Torneo> findByPropietarioIdUsuario(Long idUsuario);
    List<Torneo> findByCampeonIdEquipoOrderByIdTorneoDesc(Long idEquipo);
}
