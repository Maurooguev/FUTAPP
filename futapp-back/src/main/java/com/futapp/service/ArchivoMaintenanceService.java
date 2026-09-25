package com.futapp.service;

import com.futapp.repository.EquipoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PerfilOrganizadorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ArchivoMaintenanceService {
    private static final Pattern ARCHIVO_GENERADO = Pattern.compile(
            "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|png|webp)$");
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;
    private final PerfilOrganizadorRepository perfiles;
    private final Path carpeta;

    public ArchivoMaintenanceService(EquipoRepository equipos, JugadorRepository jugadores,
                                     PerfilOrganizadorRepository perfiles,
                                     @Value("${futapp.uploads.directory:./data/uploads}") String directory) {
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.perfiles = perfiles;
        this.carpeta = Path.of(directory).toAbsolutePath().normalize();
    }

    @Scheduled(cron = "${futapp.maintenance.upload-cleanup-cron:0 45 3 * * *}")
    @Transactional(readOnly = true)
    public void limpiarHuerfanos() {
        if (!Files.isDirectory(carpeta)) return;
        Set<String> usados = rutasUsadas();
        FileTime limite = FileTime.from(Instant.now().minus(24, ChronoUnit.HOURS));
        try (var archivos = Files.list(carpeta)) {
            archivos.filter(Files::isRegularFile).filter(this::esArchivoGenerado)
                    .filter(path -> !usados.contains(path.getFileName().toString()))
                    .filter(path -> anteriorA(path, limite))
                    .forEach(this::eliminarSilenciosamente);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo limpiar la carpeta de imágenes", exception);
        }
    }

    private Set<String> rutasUsadas() {
        Set<String> usados = new HashSet<>();
        equipos.findAll().forEach(equipo -> {
            agregar(usados, equipo.getEscudo());
            agregar(usados, equipo.getFotoPlantel());
        });
        jugadores.findAll().forEach(jugador -> agregar(usados, jugador.getFoto()));
        perfiles.findAll().forEach(perfil -> {
            agregar(usados, perfil.getFotoPerfil());
            agregar(usados, perfil.getFotoPortada());
        });
        return usados;
    }

    private void agregar(Set<String> usados, String ruta) {
        if (ruta == null || !ruta.startsWith("/uploads/")) return;
        Path nombre = Path.of(ruta.substring("/uploads/".length())).getFileName();
        if (nombre != null) usados.add(nombre.toString());
    }

    private boolean esArchivoGenerado(Path path) {
        return ARCHIVO_GENERADO.matcher(path.getFileName().toString()).matches();
    }

    private boolean anteriorA(Path path, FileTime limite) {
        try { return Files.getLastModifiedTime(path).compareTo(limite) < 0; }
        catch (IOException exception) { return false; }
    }

    private void eliminarSilenciosamente(Path path) {
        try { Files.deleteIfExists(path); }
        catch (IOException ignored) { /* Se reintentará en la próxima limpieza. */ }
    }
}
