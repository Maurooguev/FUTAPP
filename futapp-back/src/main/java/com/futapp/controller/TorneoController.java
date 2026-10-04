package com.futapp.controller;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.dto.TorneoPanelDTO;
import com.futapp.dto.EstructuraTorneoDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.dto.ContadorVisitasDTO;
import com.futapp.dto.CancelarTorneoRequestDTO;
import com.futapp.service.EstructuraCompetenciaService;
import com.futapp.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.futapp.model.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/torneos")
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

    @PostMapping("/{id}/visitas")
    public ContadorVisitasDTO registrarVisita(@PathVariable Long id) {
        return new ContadorVisitasDTO(torneoService.registrarVisita(id));
    }

    @GetMapping("/pagina")
    public PaginaDTO<TorneoResponseDTO> listarPagina(@RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int cantidad) {
        return torneoService.listarTorneosPaginados(pagina, cantidad);
    }

    @GetMapping("/buscar/pagina")
    public PaginaDTO<TorneoResponseDTO> buscarPagina(@RequestParam String q,
                                                     @RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int cantidad) {
        return torneoService.buscarPaginado(q, pagina, cantidad);
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

    @GetMapping("/campeones/equipo/{idEquipo}")
    public List<TorneoResponseDTO> titulosEquipo(@PathVariable Long idEquipo) { return torneoService.listarTitulosEquipo(idEquipo); }

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

    @PutMapping("/{id}/publicar")
    public TorneoResponseDTO publicarTorneo(@PathVariable Long id) { return torneoService.publicarTorneo(id); }

    @PutMapping("/{id}/finalizar")
    public TorneoResponseDTO finalizarTorneo(@PathVariable Long id) { return torneoService.finalizarTorneo(id); }

    @PutMapping("/{id}/cancelar")
    public TorneoResponseDTO cancelarTorneo(@PathVariable Long id,
                                             @Valid @RequestBody CancelarTorneoRequestDTO request) {
        return torneoService.cancelarTorneo(id, request.motivo());
    }

    @PutMapping("/{id}/archivar")
    public TorneoResponseDTO archivarTorneo(@PathVariable Long id) { return torneoService.archivarTorneo(id); }

    @PutMapping("/{id}/restaurar")
    public TorneoResponseDTO restaurarTorneo(@PathVariable Long id) { return torneoService.restaurarTorneo(id); }

    @DeleteMapping("/{id}")
    public String eliminarTorneo(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return "Torneo eliminado correctamente";
    }
}
