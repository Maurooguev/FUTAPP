package com.futapp.repository;

import com.futapp.model.EquipoAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoAdministradorRepository extends JpaRepository<EquipoAdministrador, Long> {
    boolean existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(Long idEquipo, Long idUsuario, String estado);
    boolean existsByEquipoIdEquipoAndEstado(Long idEquipo, String estado);
    List<EquipoAdministrador> findByUsuarioIdUsuarioAndEstado(Long idUsuario, String estado);
    List<EquipoAdministrador> findByEquipoIdEquipoAndEstado(Long idEquipo, String estado);
    List<EquipoAdministrador> findByEquipoIdEquipo(Long idEquipo);
    boolean existsByEquipoIdEquipoAndUsuarioIdUsuario(Long idEquipo, Long idUsuario);
}
