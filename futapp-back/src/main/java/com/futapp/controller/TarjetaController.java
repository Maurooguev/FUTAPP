package com.futapp.controller;

import com.futapp.dto.TarjetaRequestDTO;
import com.futapp.dto.TarjetaResponseDTO;
import com.futapp.service.TarjetaService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping
    public List<TarjetaResponseDTO> listarTarjetas() {
        return tarjetaService.listarTarjetas();
    }

    @GetMapping("/{id}")
    public TarjetaResponseDTO obtenerTarjeta(@PathVariable Long id) {
        return tarjetaService.obtenerTarjetaPorId(id);
    }

    @GetMapping("/partido/{idPartido}")
    public List<TarjetaResponseDTO> listarTarjetasPorPartido(@PathVariable Long idPartido) {
        return tarjetaService.listarTarjetasPorPartido(idPartido);
    }

    @GetMapping("/jugador/{idJugador}")
    public List<TarjetaResponseDTO> listarTarjetasPorJugador(@PathVariable Long idJugador) {
        return tarjetaService.listarTarjetasPorJugador(idJugador);
    }

    @GetMapping("/torneo/{idTorneo}")
    public List<TarjetaResponseDTO> listarTarjetasPorTorneo(@PathVariable Long idTorneo) {
        return tarjetaService.listarTarjetasPorTorneo(idTorneo);
    }

    @PostMapping
    public TarjetaResponseDTO crearTarjeta(@Valid @RequestBody TarjetaRequestDTO requestDTO) {
        return tarjetaService.crearTarjeta(requestDTO);
    }

    @PutMapping("/{id}")
    public TarjetaResponseDTO actualizarTarjeta(@PathVariable Long id, @Valid @RequestBody TarjetaRequestDTO requestDTO) {
        return tarjetaService.actualizarTarjeta(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarTarjeta(@PathVariable Long id) {
        tarjetaService.eliminarTarjeta(id);
        return "Tarjeta eliminada correctamente";
    }
}
