package com.futapp.service;

import com.futapp.dto.EstadoFavoritoDTO;
import com.futapp.dto.FavoritosDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Usuario;
import com.futapp.model.UsuarioEquipoFavorito;
import com.futapp.model.UsuarioTorneoFavorito;
import com.futapp.model.UsuarioJugadorFavorito;
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
    private final UsuarioJugadorFavoritoRepository jugadoresFavoritos;
    private final EquipoRepository equipos;
    private final TorneoRepository torneos;
    private final JugadorRepository jugadores;

    public FavoritoService(UsuarioEquipoFavoritoRepository equiposFavoritos,
                           UsuarioTorneoFavoritoRepository torneosFavoritos,
                           UsuarioJugadorFavoritoRepository jugadoresFavoritos,
                           EquipoRepository equipos, TorneoRepository torneos, JugadorRepository jugadores) {
        this.equiposFavoritos = equiposFavoritos;
        this.torneosFavoritos = torneosFavoritos;
        this.jugadoresFavoritos = jugadoresFavoritos;
        this.equipos = equipos;
        this.torneos = torneos;
        this.jugadores = jugadores;
    }

    @Transactional(readOnly = true)
    public FavoritosDTO listar() {
        Usuario usuario = actual();
        return new FavoritosDTO(
                equiposFavoritos.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                        .map(item -> item.getEquipo().getIdEquipo()).collect(Collectors.toSet()),
                torneosFavoritos.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                        .map(item -> item.getTorneo().getIdTorneo()).collect(Collectors.toSet()),
                jugadoresFavoritos.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                        .map(item -> item.getJugador().getIdJugador()).collect(Collectors.toSet()));
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

    public EstadoFavoritoDTO agregarJugador(Long idJugador) {
        Usuario usuario = actual();
        if (!jugadoresFavoritos.existsByUsuarioIdUsuarioAndJugadorIdJugador(usuario.getIdUsuario(), idJugador)) {
            var favorito = new UsuarioJugadorFavorito();
            favorito.setUsuario(usuario);
            favorito.setJugador(jugadores.findById(idJugador)
                    .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + idJugador)));
            jugadoresFavoritos.save(favorito);
        }
        return new EstadoFavoritoDTO(true);
    }

    public EstadoFavoritoDTO quitarJugador(Long idJugador) {
        jugadoresFavoritos.deleteByUsuarioIdUsuarioAndJugadorIdJugador(actual().getIdUsuario(), idJugador);
        return new EstadoFavoritoDTO(false);
    }

    private Usuario actual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new org.springframework.security.access.AccessDeniedException("Debes iniciar sesión");
    }
}
