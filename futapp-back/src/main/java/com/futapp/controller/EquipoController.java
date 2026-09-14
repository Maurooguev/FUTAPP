package com.futapp.controller;

import com.futapp.dto.EquipoRequestDTO;
import com.futapp.dto.EquipoResponseDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.service.EquipoService;
import com.futapp.service.FusionEquipoService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService equipoService;
    private final FusionEquipoService fusionEquipoService;

    public EquipoController(EquipoService equipoService, FusionEquipoService fusionEquipoService) {
        this.equipoService = equipoService;
        this.fusionEquipoService = fusionEquipoService;
    }

    @GetMapping
    public List<EquipoResponseDTO> listarEquipos() {
        return equipoService.listarEquipos();
    }

    @GetMapping("/pagina")
    public PaginaDTO<EquipoResponseDTO> listarPagina(@RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int cantidad) {
        return equipoService.listarPaginados(pagina, cantidad);
    }

    @GetMapping("/mios")
    public List<EquipoResponseDTO> listarMisEquipos() {
        return equipoService.listarMisEquipos();
    }

    @GetMapping("/creados")
    public List<EquipoResponseDTO> listarEquiposCreados() {
        return equipoService.listarEquiposCreados();
    }

    @GetMapping("/codigo/{codigo}")
    public EquipoResponseDTO buscarPorCodigo(@PathVariable String codigo) {
        return equipoService.buscarPorCodigo(codigo);
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

    @PostMapping("/{idOficial}/fusionar/{idDuplicado}")
    public void fusionarEquipos(@PathVariable Long idOficial, @PathVariable Long idDuplicado) {
        fusionEquipoService.fusionar(idOficial, idDuplicado);
    }
}
