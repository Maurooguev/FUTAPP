package com.futapp.controller;

import com.futapp.dto.PerfilOrganizadorRequestDTO;
import com.futapp.dto.PerfilOrganizadorResponseDTO;
import com.futapp.service.PerfilOrganizadorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfiles-organizador")
public class PerfilOrganizadorController {
    private final PerfilOrganizadorService service;

    public PerfilOrganizadorController(PerfilOrganizadorService service) { this.service = service; }

    @GetMapping("/{idUsuario}")
    public PerfilOrganizadorResponseDTO obtener(@PathVariable Long idUsuario) { return service.obtenerPublico(idUsuario); }

    @GetMapping("/mio")
    public PerfilOrganizadorResponseDTO mio() { return service.obtenerMio(); }

    @PostMapping("/mio")
    public PerfilOrganizadorResponseDTO crear(@Valid @RequestBody PerfilOrganizadorRequestDTO request) { return service.crear(request); }

    @PutMapping("/mio")
    public PerfilOrganizadorResponseDTO actualizar(@Valid @RequestBody PerfilOrganizadorRequestDTO request) { return service.actualizar(request); }
}
