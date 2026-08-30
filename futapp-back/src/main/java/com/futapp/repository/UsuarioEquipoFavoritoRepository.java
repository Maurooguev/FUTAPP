package com.futapp.repository;

import com.futapp.model.UsuarioEquipoFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioEquipoFavoritoRepository extends JpaRepository<UsuarioEquipoFavorito, Long> {
    List<UsuarioEquipoFavorito> findByUsuarioIdUsuario(Long idUsuario);
    boolean existsByUsuarioIdUsuarioAndEquipoIdEquipo(Long idUsuario, Long idEquipo);
    void deleteByUsuarioIdUsuarioAndEquipoIdEquipo(Long idUsuario, Long idEquipo);
}
