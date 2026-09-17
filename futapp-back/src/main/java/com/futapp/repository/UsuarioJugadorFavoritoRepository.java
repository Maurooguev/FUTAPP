package com.futapp.repository;

import com.futapp.model.UsuarioJugadorFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioJugadorFavoritoRepository extends JpaRepository<UsuarioJugadorFavorito, Long> {
    List<UsuarioJugadorFavorito> findByUsuarioIdUsuario(Long idUsuario);
    boolean existsByUsuarioIdUsuarioAndJugadorIdJugador(Long idUsuario, Long idJugador);
    void deleteByUsuarioIdUsuarioAndJugadorIdJugador(Long idUsuario, Long idJugador);
}
