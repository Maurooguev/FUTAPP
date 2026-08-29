package com.futapp.controller;

import com.futapp.dto.PartidoRequestDTO;
import com.futapp.dto.PartidoResponseDTO;
import com.futapp.service.PartidoService;
import org.springframework.web.bind.annotation.*;
import com.futapp.dto.ResultadoPartidoDTO;
import com.futapp.dto.FixtureRequestDTO;
import com.futapp.dto.MvpPartidoRequestDTO;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/partidos")
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @GetMapping
    public List<PartidoResponseDTO> listarPartidos() {
        return partidoService.listarPartidos();
    }

    @GetMapping("/{id}")
    public PartidoResponseDTO obtenerPartido(@PathVariable Long id) {
        return partidoService.obtenerPartidoPorId(id);
    }

    @GetMapping("/torneo/{idTorneo}")
    public List<PartidoResponseDTO> listarPartidosPorTorneo(@PathVariable Long idTorneo) {
        return partidoService.listarPartidosPorTorneo(idTorneo);
    }

    @GetMapping("/fecha/{idFecha}")
    public List<PartidoResponseDTO> listarPartidosPorFecha(@PathVariable Long idFecha) {
        return partidoService.listarPartidosPorFecha(idFecha);
    }

    @PostMapping
    public PartidoResponseDTO crearPartido(@Valid @RequestBody PartidoRequestDTO requestDTO) {
        return partidoService.crearPartido(requestDTO);
    }

    @PostMapping("/torneo/{idTorneo}/generar-fixture")
    public List<PartidoResponseDTO> generarFixture(@PathVariable Long idTorneo,
                                                   @Valid @RequestBody FixtureRequestDTO request) {
        return partidoService.generarFixture(idTorneo, request);
    }

    @PutMapping("/{id}")
    public PartidoResponseDTO actualizarPartido(@PathVariable Long id, @Valid @RequestBody PartidoRequestDTO requestDTO) {
        return partidoService.actualizarPartido(id, requestDTO);
        
    }

    @PutMapping("/{id}/resultado")
    public PartidoResponseDTO cargarResultado(
            @PathVariable Long id,
            @RequestBody ResultadoPartidoDTO dto) {

        return partidoService.cargarResultado(id, dto);
    }

    @PutMapping("/{id}/mvp")
    public PartidoResponseDTO elegirMvp(@PathVariable Long id, @RequestBody MvpPartidoRequestDTO request) {
        return partidoService.elegirMvp(id, request);
    }

    @DeleteMapping("/{id}")
    public String eliminarPartido(@PathVariable Long id) {
        partidoService.eliminarPartido(id);
        return "Partido eliminado correctamente";
    }

    @DeleteMapping("/torneo/{idTorneo}/fixture")
    public void eliminarFixture(@PathVariable Long idTorneo) {
        partidoService.eliminarFixture(idTorneo);
    }
}
