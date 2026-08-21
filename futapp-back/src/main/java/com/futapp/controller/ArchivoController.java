package com.futapp.controller;

import com.futapp.service.ArchivoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/archivos")
public class ArchivoController {
    private final ArchivoService archivos;

    public ArchivoController(ArchivoService archivos) { this.archivos = archivos; }

    @PostMapping(value = "/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> subirImagen(@RequestPart("archivo") MultipartFile archivo) {
        return Map.of("url", archivos.guardarImagen(archivo));
    }
}
