package com.futapp.dto;
import java.time.LocalDateTime;
public record ReclamoResponseDTO(Long idSolicitud, String tipo, String estado, Long idSolicitante,
 String nombreSolicitante, Long idEquipo, String nombreEquipo, Long idJugador, String nombreJugador,
 Long idTorneo, String nombreTorneo, String mensaje, String motivoResolucion,
 LocalDateTime fechaSolicitud, LocalDateTime fechaResolucion) {}
