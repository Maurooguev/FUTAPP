package com.futapp.service;

import com.futapp.dto.EstadoFavoritoDTO;
import com.futapp.dto.FavoritosDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Usuario;
import com.futapp.model.UsuarioEquipoFavorito;
import com.futapp.model.UsuarioTorneoFavorito;
import com.futapp.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class FavoritoService {
    private final UsuarioEquipoFavoritoRepository equiposFavoritos;
    private final UsuarioTorneoFavoritoRepository torneosFavoritos;
    private final EquipoRepository equipos;
    private final TorneoRepository torneos;

    public FavoritoService(UsuarioEquipoFavoritoRepository equiposFavoritos,
                           UsuarioTorneoFavoritoRepository torneosFavoritos,
                           EquipoRepository equipos, TorneoRepository torneos) {
        this.equiposFavoritos = equiposFavoritos;
        this.torneosFavoritos = torneosFavoritos;
        this.equipos = equipos;
        this.torneos = torneos;
    }

    @Transactional(readOnly = true)
    public FavoritosDTO listar() {
        Usuario usuario = actual();
        return new FavoritosDTO(
                equiposFavoritos.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                        .map(item -> item.getEquipo().getIdEquipo()).collect(Collectors.toSet()),
                torneosFavoritos.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                        .map(item -> item.getTorneo().getIdTorneo()).collect(Collectors.toSet()));
    }

    public EstadoFavoritoDTO agregarEquipo(Long idEquipo) {
        Usuario usuario = actual();
        if (!equiposFavoritos.existsByUsuarioIdUsuarioAndEquipoIdEquipo(usuario.getIdUsuario(), idEquipo)) {
            var favorito = new UsuarioEquipoFavorito();
            favorito.setUsuario(usuario);
            favorito.setEquipo(equipos.findById(idEquipo)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + idEquipo)));
            equiposFavoritos.save(favorito);
        }
        return new EstadoFavoritoDTO(true);
    }

    public EstadoFavoritoDTO quitarEquipo(Long idEquipo) {
        equiposFavoritos.deleteByUsuarioIdUsuarioAndEquipoIdEquipo(actual().getIdUsuario(), idEquipo);
        return new EstadoFavoritoDTO(false);
    }

    public EstadoFavoritoDTO agregarTorneo(Long idTorneo) {
        Usuario usuario = actual();
        if (!torneosFavoritos.existsByUsuarioIdUsuarioAndTorneoIdTorneo(usuario.getIdUsuario(), idTorneo)) {
            var favorito = new UsuarioTorneoFavorito();
            favorito.setUsuario(usuario);
            favorito.setTorneo(torneos.findById(idTorneo)
                    .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo)));
            torneosFavoritos.save(favorito);
        }
        return new EstadoFavoritoDTO(true);
    }

    public EstadoFavoritoDTO quitarTorneo(Long idTorneo) {
        torneosFavoritos.deleteByUsuarioIdUsuarioAndTorneoIdTorneo(actual().getIdUsuario(), idTorneo);
        return new EstadoFavoritoDTO(false);
    }

    private Usuario actual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new org.springframework.security.access.AccessDeniedException("Debes iniciar sesión");
    }
}
