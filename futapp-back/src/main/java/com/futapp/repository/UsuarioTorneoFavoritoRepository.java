package com.futapp.repository;

import com.futapp.model.UsuarioTorneoFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioTorneoFavoritoRepository extends JpaRepository<UsuarioTorneoFavorito, Long> {
    List<UsuarioTorneoFavorito> findByUsuarioIdUsuario(Long idUsuario);
    boolean existsByUsuarioIdUsuarioAndTorneoIdTorneo(Long idUsuario, Long idTorneo);
    void deleteByUsuarioIdUsuarioAndTorneoIdTorneo(Long idUsuario, Long idTorneo);
}
