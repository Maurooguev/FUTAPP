package com.futapp.controller;

import com.futapp.dto.EquipoRequestDTO;
import com.futapp.dto.EquipoResponseDTO;
import com.futapp.service.EquipoService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public List<EquipoResponseDTO> listarEquipos() {
        return equipoService.listarEquipos();
    }

    @GetMapping("/{id}")
    public EquipoResponseDTO obtenerEquipo(@PathVariable Long id) {
        return equipoService.obtenerEquipoPorId(id);
    }

    @GetMapping("/{id}/permisos")
    public Map<String, Boolean> obtenerPermisos(@PathVariable Long id) {
        return Map.of("puedeGestionar", equipoService.puedeGestionar(id));
    }

    @GetMapping("/torneo/{idTorneo}")
    public List<EquipoResponseDTO> listarEquiposPorTorneo(@PathVariable Long idTorneo) {
        return equipoService.listarEquiposPorTorneo(idTorneo);
    }

    @PostMapping
    public EquipoResponseDTO crearEquipo(@Valid @RequestBody EquipoRequestDTO requestDTO) {
        return equipoService.crearEquipo(requestDTO);
    }

    @PutMapping("/{id}")
    public EquipoResponseDTO actualizarEquipo(@PathVariable Long id, @Valid @RequestBody EquipoRequestDTO requestDTO) {
        return equipoService.actualizarEquipo(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarEquipo(@PathVariable Long id) {
        equipoService.eliminarEquipo(id);
        return "Equipo eliminado correctamente";
    }
}
