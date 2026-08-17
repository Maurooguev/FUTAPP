package com.futapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/hola")
    public String saludar() {
        return "FUTAPP funcionando";
    }

    @GetMapping("/status")
    public String status() {
        return "Servidor activo";
    }
}