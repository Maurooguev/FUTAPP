package com.futapp.controller;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.dto.TorneoPanelDTO;
import com.futapp.dto.EstructuraTorneoDTO;
import com.futapp.service.EstructuraCompetenciaService;
import com.futapp.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.futapp.model.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/torneos")
@CrossOrigin(origins = "http://localhost:5173")
public class TorneoController {

    private final TorneoService torneoService;
    private final EstructuraCompetenciaService estructuraCompetencia;

    public TorneoController(TorneoService torneoService, EstructuraCompetenciaService estructuraCompetencia) {
        this.torneoService = torneoService;
        this.estructuraCompetencia = estructuraCompetencia;
    }

    @GetMapping
    public List<TorneoResponseDTO> listarTorneos() {
        return torneoService.listarTorneos();
    }

    @GetMapping("/{id}")
    public TorneoResponseDTO obtenerTorneo(@PathVariable Long id) {
        return torneoService.obtenerTorneoPorId(id);
    }

    @GetMapping("/{id}/estructura")
    public EstructuraTorneoDTO obtenerEstructura(@PathVariable Long id) {
        return estructuraCompetencia.obtener(id);
    }

    @GetMapping("/buscar")
    public List<TorneoResponseDTO> buscar(@RequestParam String q) { return torneoService.buscar(q.trim()); }

    @GetMapping("/mios")
    public List<TorneoResponseDTO> misTorneos(@AuthenticationPrincipal Usuario usuario) { return torneoService.listarPropios(usuario); }

    @GetMapping("/mios/panel")
    public List<TorneoPanelDTO> miPanel(@AuthenticationPrincipal Usuario usuario) { return torneoService.obtenerPanel(usuario); }

    @PostMapping
    public TorneoResponseDTO crearTorneo(@Valid @RequestBody TorneoRequestDTO requestDTO,
                                         @AuthenticationPrincipal Usuario usuario) {
        return torneoService.crearTorneo(requestDTO, usuario);
    }

    @PutMapping("/{id}")
    public TorneoResponseDTO actualizarTorneo(@PathVariable Long id, @Valid @RequestBody TorneoRequestDTO requestDTO) {
        return torneoService.actualizarTorneo(id, requestDTO);
    }

    @PutMapping("/{id}/iniciar")
    public TorneoResponseDTO iniciarTorneo(@PathVariable Long id) { return torneoService.iniciarTorneo(id); }

    @PutMapping("/{id}/finalizar")
    public TorneoResponseDTO finalizarTorneo(@PathVariable Long id) { return torneoService.finalizarTorneo(id); }

    @DeleteMapping("/{id}")
    public String eliminarTorneo(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return "Torneo eliminado correctamente";
    }
}
