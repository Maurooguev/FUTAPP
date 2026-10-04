package com.futapp.service;

import com.futapp.model.Equipo;
import com.futapp.model.PerfilOrganizador;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.PerfilOrganizadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EscudoNormalizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EscudoNormalizationService.class);
    private static final double COLOR_TOLERANCE = 54.0;
    private static final double FEATHER_RANGE = 28.0;
    private final EquipoRepository equipos;
    private final PerfilOrganizadorRepository perfilesOrganizador;
    private final Path uploads;
    private final Path respaldos;

    public EscudoNormalizationService(EquipoRepository equipos, PerfilOrganizadorRepository perfilesOrganizador,
                                      @Value("${futapp.uploads.directory:./data/uploads}") String directory) {
        this.equipos = equipos;
        this.perfilesOrganizador = perfilesOrganizador;
        this.uploads = Path.of(directory).toAbsolutePath().normalize();
        this.respaldos = this.uploads.resolveSibling("upload-originals").normalize();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void normalizarEscudosExistentes() {
        if (!Files.isDirectory(uploads)) return;
        Map<String, String> procesados = new HashMap<>();
        int actualizados = 0;
        for (Equipo equipo : equipos.findAll()) {
            String actual = equipo.getEscudo();
            if (actual == null || !actual.startsWith("/uploads/")) continue;
            String normalizado = procesados.computeIfAbsent(actual, this::normalizarRuta);
            if (normalizado == null || normalizado.equals(actual)) continue;
            equipo.setEscudo(normalizado);
            actualizados += 1;
        }
        for (PerfilOrganizador perfil : perfilesOrganizador.findAll()) {
            String actual = perfil.getFotoPerfil();
            if (actual == null || !actual.startsWith("/uploads/")) continue;
            String normalizado = procesados.computeIfAbsent(actual, this::normalizarRuta);
            if (normalizado == null || normalizado.equals(actual)) continue;
            perfil.setFotoPerfil(normalizado);
            actualizados += 1;
        }
        if (actualizados > 0) LOGGER.info("Escudos y logos normalizados con fondo transparente: {}", actualizados);
    }

    private String normalizarRuta(String rutaPublica) {
        Path nombre = Path.of(rutaPublica.substring("/uploads/".length())).getFileName();
        if (nombre == null) return rutaPublica;
        Path origen = uploads.resolve(nombre).normalize();
        if (!origen.startsWith(uploads) || !Files.isRegularFile(origen)) return rutaPublica;
        try {
            BufferedImage original = ImageIO.read(origen.toFile());
            if (original == null || yaTieneTransparencia(original)) return rutaPublica;
            BufferedImage resultado = quitarFondoConectado(original);
            if (resultado == null) {
                LOGGER.warn("No se encontró un fondo uniforme en el escudo {}", nombre);
                return rutaPublica;
            }
            Files.createDirectories(uploads);
            Files.createDirectories(respaldos);
            Path respaldo = respaldos.resolve(nombre).normalize();
            if (respaldo.startsWith(respaldos) && !Files.exists(respaldo)) {
                Files.copy(origen, respaldo, StandardCopyOption.COPY_ATTRIBUTES);
            }
            String nuevoNombre = UUID.randomUUID() + ".png";
            Path destino = uploads.resolve(nuevoNombre).normalize();
            if (!destino.startsWith(uploads) || !ImageIO.write(resultado, "png", destino.toFile())) {
                throw new IOException("No se pudo escribir el PNG transparente");
            }
            return "/uploads/" + nuevoNombre;
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("No se pudo normalizar el escudo {}: {}", nombre, exception.getMessage());
            return rutaPublica;
        }
    }

    private boolean yaTieneTransparencia(BufferedImage imagen) {
        if (!imagen.getColorModel().hasAlpha()) return false;
        int transparentes = 0;
        int total = imagen.getWidth() * imagen.getHeight();
        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                if (((imagen.getRGB(x, y) >>> 24) & 255) < 245) transparentes += 1;
            }
        }
        return transparentes > total * 0.005;
    }

    private BufferedImage quitarFondoConectado(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        int sampleSize = Math.max(2, Math.min(12, Math.min(width, height) / 25));
        long red = 0; long green = 0; long blue = 0; long samples = 0;
        int[][] corners = {{0, 0}, {width - sampleSize, 0}, {0, height - sampleSize}, {width - sampleSize, height - sampleSize}};
        for (int[] corner : corners) {
            for (int y = corner[1]; y < corner[1] + sampleSize; y++) {
                for (int x = corner[0]; x < corner[0] + sampleSize; x++) {
                    int argb = original.getRGB(x, y);
                    if (((argb >>> 24) & 255) < 20) continue;
                    red += (argb >>> 16) & 255; green += (argb >>> 8) & 255; blue += argb & 255; samples += 1;
                }
            }
        }
        if (samples == 0) return null;
        double[] background = {red / (double) samples, green / (double) samples, blue / (double) samples};
        boolean[] visited = new boolean[width * height];
        int[] queue = new int[width * height];
        int tail = 0;
        for (int x = 0; x < width; x++) {
            tail = enqueue(original, x, visited, queue, tail, background);
            tail = enqueue(original, (height - 1) * width + x, visited, queue, tail, background);
        }
        for (int y = 1; y < height - 1; y++) {
            tail = enqueue(original, y * width, visited, queue, tail, background);
            tail = enqueue(original, y * width + width - 1, visited, queue, tail, background);
        }
        for (int head = 0; head < tail; head++) {
            int index = queue[head]; int x = index % width; int y = index / width;
            if (x > 0) tail = enqueue(original, index - 1, visited, queue, tail, background);
            if (x + 1 < width) tail = enqueue(original, index + 1, visited, queue, tail, background);
            if (y > 0) tail = enqueue(original, index - width, visited, queue, tail, background);
            if (y + 1 < height) tail = enqueue(original, index + width, visited, queue, tail, background);
        }
        BufferedImage resultado = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int eliminados = 0;
        for (int index = 0; index < visited.length; index++) {
            int x = index % width; int y = index / width; int argb = original.getRGB(x, y);
            if (visited[index]) {
                double distance = colorDistance(argb, background);
                double factor = Math.max(0, Math.min(1, (distance - COLOR_TOLERANCE) / FEATHER_RANGE));
                int alpha = (int) Math.round(((argb >>> 24) & 255) * factor);
                if (alpha < ((argb >>> 24) & 255)) eliminados += 1;
                argb = (alpha << 24) | (argb & 0x00ffffff);
            }
            resultado.setRGB(x, y, argb);
        }
        return eliminados > width * height * 0.005 ? resultado : null;
    }

    private int enqueue(BufferedImage image, int index, boolean[] visited, int[] queue, int tail, double[] background) {
        if (visited[index]) return tail;
        int x = index % image.getWidth(); int y = index / image.getWidth(); int argb = image.getRGB(x, y);
        int alpha = (argb >>> 24) & 255;
        if (alpha > 20 && colorDistance(argb, background) > COLOR_TOLERANCE + FEATHER_RANGE) return tail;
        visited[index] = true;
        queue[tail] = index;
        return tail + 1;
    }

    private double colorDistance(int argb, double[] background) {
        double red = ((argb >>> 16) & 255) - background[0];
        double green = ((argb >>> 8) & 255) - background[1];
        double blue = (argb & 255) - background[2];
        return Math.sqrt(red * red + green * green + blue * blue);
    }
}
