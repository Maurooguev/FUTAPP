package com.futapp.controller;
import com.futapp.dto.*; import com.futapp.service.PerfilJugadorService; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/perfil-jugador")
public class PerfilJugadorController { private final PerfilJugadorService service; public PerfilJugadorController(PerfilJugadorService s){service=s;} @GetMapping("/mio") public PerfilJugadorResponseDTO mio(){return service.mio();} @PutMapping("/mio") public PerfilJugadorResponseDTO actualizar(@Valid @RequestBody PerfilJugadorPersonalDTO r){return service.actualizar(r);} }
