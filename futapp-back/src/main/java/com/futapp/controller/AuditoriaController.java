package com.futapp.controller;

import com.futapp.dto.AuditoriaAdministrativaDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.service.AuditoriaAdministrativaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {
    private final AuditoriaAdministrativaService auditoria;
    public AuditoriaController(AuditoriaAdministrativaService auditoria) { this.auditoria = auditoria; }

    @GetMapping
    public PaginaDTO<AuditoriaAdministrativaDTO> listar(@RequestParam(defaultValue = "0") int pagina,
                                                        @RequestParam(defaultValue = "25") int cantidad) {
        return auditoria.listar(pagina, cantidad);
    }
}
