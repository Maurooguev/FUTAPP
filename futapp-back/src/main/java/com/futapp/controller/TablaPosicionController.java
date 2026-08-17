package com.futapp.controller;

import com.futapp.dto.TablaPosicionDTO;
import com.futapp.service.TablaPosicionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/torneos")
public class TablaPosicionController {

    private final TablaPosicionService tablaPosicionService;

    public TablaPosicionController(TablaPosicionService tablaPosicionService) {
        this.tablaPosicionService = tablaPosicionService;
    }

    @GetMapping("/{idTorneo}/tabla")
    public List<TablaPosicionDTO> obtenerTabla(@PathVariable Long idTorneo) {
        return tablaPosicionService.obtenerTablaPorTorneo(idTorneo);
    }
}