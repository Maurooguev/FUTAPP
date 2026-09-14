package com.futapp;

import com.futapp.service.ArchivoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivoServiceTests {
    @TempDir Path carpeta;

    @Test
    void guardaUnaImagenConNombreSeguro() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile imagen = new MockMultipartFile("archivo", "foto.png", "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3});

        String url = service.guardarImagen(imagen);

        assertTrue(url.matches("/uploads/[a-f0-9-]+\\.png"));
        assertTrue(Files.exists(carpeta.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void rechazaArchivosQueNoSonImagen() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "datos.txt", "text/plain", new byte[]{1});

        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void rechazaUnArchivoFalsoAunqueDeclareSerImagen() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "falso.png", "image/png", "no soy una imagen".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }
}
