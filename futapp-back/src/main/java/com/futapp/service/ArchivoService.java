package com.futapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ArchivoService {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final Path carpeta;

    public ArchivoService(@Value("${futapp.uploads.directory:./data/uploads}") String directory) {
        this.carpeta = Path.of(directory).toAbsolutePath().normalize();
    }

    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Seleccioná una imagen");
        if (archivo.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        String extension = EXTENSIONES.get(archivo.getContentType());
        if (extension == null) throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WebP");
        String nombre = UUID.randomUUID() + extension;
        try {
            byte[] contenido = archivo.getBytes();
            if (!firmaValida(contenido, archivo.getContentType())) throw new IllegalArgumentException("El contenido del archivo no corresponde a una imagen válida");
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve(nombre).normalize();
            if (!destino.startsWith(carpeta)) throw new IllegalArgumentException("Nombre de archivo inválido");
            Files.write(destino, contenido);
            return "/uploads/" + nombre;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo guardar la imagen", exception);
        }
    }

    private boolean firmaValida(byte[] b, String tipo) {
        if ("image/jpeg".equals(tipo)) return b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255;
        if ("image/png".equals(tipo)) return b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10;
        return b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
