package com.futapp.controller;

import com.futapp.dto.FechaRequestDTO;
import com.futapp.dto.FechaResponseDTO;
import com.futapp.service.FechaService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/fechas")
public class FechaController {

    private final FechaService fechaService;

    public FechaController(FechaService fechaService) {
        this.fechaService = fechaService;
    }

    @GetMapping
    public List<FechaResponseDTO> listarFechas() {
        return fechaService.listarFechas();
    }

    @GetMapping("/{id}")
    public FechaResponseDTO obtenerFecha(@PathVariable Long id) {
        return fechaService.obtenerFechaPorId(id);
    }

    @GetMapping("/torneo/{idTorneo}")
    public List<FechaResponseDTO> listarFechasPorTorneo(@PathVariable Long idTorneo) {
        return fechaService.listarFechasPorTorneo(idTorneo);
    }

    @PostMapping
    public FechaResponseDTO crearFecha(@Valid @RequestBody FechaRequestDTO requestDTO) {
        return fechaService.crearFecha(requestDTO);
    }

    @PutMapping("/{id}")
    public FechaResponseDTO actualizarFecha(@PathVariable Long id, @Valid @RequestBody FechaRequestDTO requestDTO) {
        return fechaService.actualizarFecha(id, requestDTO);
    }

    @PutMapping("/{id}/cerrar")
    public FechaResponseDTO cerrarFecha(@PathVariable Long id) {
        return fechaService.cerrarFecha(id);
    }

    @DeleteMapping("/{id}")
    public String eliminarFecha(@PathVariable Long id) {
        fechaService.eliminarFecha(id);
        return "Fecha eliminada correctamente";
    }
}
