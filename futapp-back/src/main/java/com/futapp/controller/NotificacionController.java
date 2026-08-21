package com.futapp.controller;
import com.futapp.dto.NotificacionResponseDTO;
import com.futapp.service.NotificacionService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/notificaciones")
public class NotificacionController {
    private final NotificacionService service;
    public NotificacionController(NotificacionService service) { this.service = service; }
    @GetMapping public List<NotificacionResponseDTO> listar() { return service.listar(); }
    @GetMapping("/no-leidas/count") public Map<String,Long> noLeidas() { return Map.of("cantidad", service.noLeidas()); }
    @PutMapping("/{id}/leer") public NotificacionResponseDTO leer(@PathVariable Long id) { return service.marcarLeida(id); }
    @PutMapping("/leer-todas") public void leerTodas() { service.marcarTodasLeidas(); }
}
