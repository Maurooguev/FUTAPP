package com.futapp.controller;

import com.futapp.dto.EstadoFavoritoDTO;
import com.futapp.dto.FavoritosDTO;
import com.futapp.service.FavoritoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {
    private final FavoritoService favoritos;
    public FavoritoController(FavoritoService favoritos) { this.favoritos = favoritos; }
    @GetMapping public FavoritosDTO listar() { return favoritos.listar(); }
    @PutMapping("/equipos/{id}") public EstadoFavoritoDTO agregarEquipo(@PathVariable Long id) { return favoritos.agregarEquipo(id); }
    @DeleteMapping("/equipos/{id}") public EstadoFavoritoDTO quitarEquipo(@PathVariable Long id) { return favoritos.quitarEquipo(id); }
    @PutMapping("/torneos/{id}") public EstadoFavoritoDTO agregarTorneo(@PathVariable Long id) { return favoritos.agregarTorneo(id); }
    @DeleteMapping("/torneos/{id}") public EstadoFavoritoDTO quitarTorneo(@PathVariable Long id) { return favoritos.quitarTorneo(id); }
    @PutMapping("/jugadores/{id}") public EstadoFavoritoDTO agregarJugador(@PathVariable Long id) { return favoritos.agregarJugador(id); }
    @DeleteMapping("/jugadores/{id}") public EstadoFavoritoDTO quitarJugador(@PathVariable Long id) { return favoritos.quitarJugador(id); }
}
