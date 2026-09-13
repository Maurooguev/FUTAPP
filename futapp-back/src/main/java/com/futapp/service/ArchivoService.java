package com.futapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ArchivoService {
    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final Path carpeta;

    public ArchivoService(@Value("${futapp.uploads.directory:./data/uploads}") String directory) {
        this.carpeta = Path.of(directory).toAbsolutePath().normalize();
    }

    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Seleccioná una imagen");
        if (archivo.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("La imagen no puede superar los 8 MB");
        String extension = EXTENSIONES.get(archivo.getContentType());
        if (extension == null) throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WebP");
        String nombre = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve(nombre).normalize();
            if (!destino.startsWith(carpeta)) throw new IllegalArgumentException("Nombre de archivo inválido");
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + nombre;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo guardar la imagen", exception);
        }
    }
}
