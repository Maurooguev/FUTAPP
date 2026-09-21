package com.futapp.controller;

import com.futapp.dto.BusquedaGlobalDTO;
import com.futapp.service.BusquedaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/busqueda")
public class BusquedaController {
    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @GetMapping
    public BusquedaGlobalDTO buscar(@RequestParam(defaultValue = "") String q) {
        return busquedaService.buscar(q);
    }
}
