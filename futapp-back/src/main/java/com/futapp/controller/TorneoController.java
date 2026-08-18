package com.futapp.controller;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
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

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public List<TorneoResponseDTO> listarTorneos() {
        return torneoService.listarTorneos();
    }

    @GetMapping("/{id}")
    public TorneoResponseDTO obtenerTorneo(@PathVariable Long id) {
        return torneoService.obtenerTorneoPorId(id);
    }

    @GetMapping("/buscar")
    public List<TorneoResponseDTO> buscar(@RequestParam String q) { return torneoService.buscar(q.trim()); }

    @GetMapping("/mios")
    public List<TorneoResponseDTO> misTorneos(@AuthenticationPrincipal Usuario usuario) { return torneoService.listarPropios(usuario); }

    @PostMapping
    public TorneoResponseDTO crearTorneo(@Valid @RequestBody TorneoRequestDTO requestDTO,
                                         @AuthenticationPrincipal Usuario usuario) {
        return torneoService.crearTorneo(requestDTO, usuario);
    }

    @PutMapping("/{id}")
    public TorneoResponseDTO actualizarTorneo(@PathVariable Long id, @Valid @RequestBody TorneoRequestDTO requestDTO) {
        return torneoService.actualizarTorneo(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarTorneo(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return "Torneo eliminado correctamente";
    }
}
