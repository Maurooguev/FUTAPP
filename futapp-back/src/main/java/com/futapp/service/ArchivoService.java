package com.futapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ArchivoService {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_PIXELS = 16_777_216L;
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final Path carpeta;

    public ArchivoService(@Value("${futapp.uploads.directory:./data/uploads}") String directory) {
        this.carpeta = Path.of(directory).toAbsolutePath().normalize();
    }

    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Seleccioná una imagen");
        if (archivo.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        try {
            byte[] contenido = archivo.getBytes();
            String tipoReal = detectarTipo(contenido);
            if (tipoReal == null || !tipoReal.equals(archivo.getContentType())) {
                throw new IllegalArgumentException("El contenido del archivo no corresponde al formato declarado");
            }
            try {
                validarImagen(contenido, tipoReal);
            } catch (IOException exception) {
                throw new IllegalArgumentException("La imagen está dañada o incompleta", exception);
            }
            Files.createDirectories(carpeta);
            String nombre = UUID.randomUUID() + EXTENSIONES.get(tipoReal);
            Path destino = carpeta.resolve(nombre).normalize();
            if (!destino.startsWith(carpeta)) throw new IllegalArgumentException("Nombre de archivo inválido");
            Path temporal = Files.createTempFile(carpeta, "upload-", ".tmp");
            try {
                Files.write(temporal, contenido);
                try {
                    Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                    Files.move(temporal, destino);
                }
            } finally {
                Files.deleteIfExists(temporal);
            }
            return "/uploads/" + nombre;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo guardar la imagen", exception);
        }
    }

    private String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "image/jpeg";
        if (b.length >= 24 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71
                && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10
                && b[12] == 'I' && b[13] == 'H' && b[14] == 'D' && b[15] == 'R') return "image/png";
        if (b.length >= 30 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        return null;
    }

    private void validarImagen(byte[] contenido, String tipo) throws IOException {
        int[] dimensiones = "image/webp".equals(tipo)
                ? dimensionesWebp(contenido)
                : dimensionesDecodificadas(contenido, tipo);
        int ancho = dimensiones[0], alto = dimensiones[1];
        if (ancho <= 0 || alto <= 0 || ancho > MAX_DIMENSION || alto > MAX_DIMENSION
                || (long) ancho * alto > MAX_PIXELS) {
            throw new IllegalArgumentException("La imagen no puede superar 4096 × 4096 píxeles");
        }
    }

    private int[] dimensionesDecodificadas(byte[] contenido, String tipo) throws IOException {
        ImageIO.setUseCache(false);
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("No pudimos interpretar la imagen");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int ancho = reader.getWidth(0), alto = reader.getHeight(0);
                if (ancho > MAX_DIMENSION || alto > MAX_DIMENSION || (long) ancho * alto > MAX_PIXELS) {
                    throw new IllegalArgumentException("La imagen no puede superar 4096 × 4096 píxeles");
                }
                BufferedImage imagen = reader.read(0);
                if (imagen == null) throw new IllegalArgumentException("La imagen está dañada o incompleta");
                String formato = reader.getFormatName().toLowerCase();
                if (("image/png".equals(tipo) && !formato.contains("png"))
                        || ("image/jpeg".equals(tipo) && !formato.contains("jpeg") && !formato.contains("jpg"))) {
                    throw new IllegalArgumentException("El formato real de la imagen no coincide");
                }
                return new int[]{ancho, alto};
            } finally {
                reader.dispose();
            }
        }
    }

    private int[] dimensionesWebp(byte[] b) {
        long longitudRiff = littleEndian32(b, 4) + 8;
        if (longitudRiff != b.length || longitudRiff < 30) throw new IllegalArgumentException("La imagen WebP está dañada o incompleta");
        int[] dimensiones = null;
        boolean contieneImagen = false;
        int offset = 12;
        while (offset + 8 <= b.length) {
            String chunk = new String(b, offset, 4, java.nio.charset.StandardCharsets.US_ASCII);
            long largoChunk = littleEndian32(b, offset + 4);
            long inicioDatos = offset + 8L;
            long finDatos = inicioDatos + largoChunk;
            if (finDatos > b.length) throw new IllegalArgumentException("La imagen WebP está dañada o incompleta");
            int datos = (int) inicioDatos;
            if ("ANIM".equals(chunk) || "ANMF".equals(chunk)) {
                throw new IllegalArgumentException("Las imágenes WebP animadas no están permitidas");
            }
            if ("VP8X".equals(chunk)) {
                if (largoChunk < 10 || (b[datos] & 0x02) != 0) throw new IllegalArgumentException("La estructura de la imagen WebP no es válida");
                dimensiones = new int[]{littleEndian24(b, datos + 4) + 1, littleEndian24(b, datos + 7) + 1};
            } else if ("VP8L".equals(chunk)) {
                if (largoChunk < 5 || (b[datos] & 255) != 0x2f) throw new IllegalArgumentException("La estructura de la imagen WebP no es válida");
                int bits = (b[datos + 1] & 255) | ((b[datos + 2] & 255) << 8)
                        | ((b[datos + 3] & 255) << 16) | ((b[datos + 4] & 255) << 24);
                if (dimensiones == null) dimensiones = new int[]{(bits & 0x3fff) + 1, ((bits >> 14) & 0x3fff) + 1};
                contieneImagen = true;
            } else if ("VP8 ".equals(chunk)) {
                if (largoChunk < 10 || (b[datos + 3] & 255) != 0x9d || b[datos + 4] != 0x01 || b[datos + 5] != 0x2a) {
                    throw new IllegalArgumentException("La estructura de la imagen WebP no es válida");
                }
                if (dimensiones == null) dimensiones = new int[]{littleEndian16(b, datos + 6) & 0x3fff, littleEndian16(b, datos + 8) & 0x3fff};
                contieneImagen = true;
            }
            offset = (int) (finDatos + (largoChunk & 1));
        }
        if (offset != b.length || !contieneImagen || dimensiones == null) {
            throw new IllegalArgumentException("La estructura de la imagen WebP no es válida");
        }
        return dimensiones;
    }

    private long littleEndian32(byte[] b, int offset) {
        return Integer.toUnsignedLong((b[offset] & 255) | ((b[offset + 1] & 255) << 8)
                | ((b[offset + 2] & 255) << 16) | ((b[offset + 3] & 255) << 24));
    }

    private int littleEndian24(byte[] b, int offset) {
        return (b[offset] & 255) | ((b[offset + 1] & 255) << 8) | ((b[offset + 2] & 255) << 16);
    }

    private int littleEndian16(byte[] b, int offset) {
        return (b[offset] & 255) | ((b[offset + 1] & 255) << 8);
    }
}
