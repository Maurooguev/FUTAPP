package com.futapp.controller;

import com.futapp.dto.ExplorarDTO;
import com.futapp.service.ExplorarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/explorar")
public class ExplorarController {
    private final ExplorarService explorarService;

    public ExplorarController(ExplorarService explorarService) {
        this.explorarService = explorarService;
    }

    @GetMapping
    public ExplorarDTO obtener() {
        return explorarService.obtener();
    }
}
