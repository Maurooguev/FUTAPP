package com.futapp.dto;
import java.time.LocalDateTime;
public record ReclamoResponseDTO(Long idSolicitud, String tipo, String estado, Long idSolicitante,
 String nombreSolicitante, String emailSolicitante, String rolSolicitante, String fotoSolicitante,
 Long idEquipo, String nombreEquipo, String escudoEquipo, Long idJugador, String nombreJugador,
 Long idEquipoDestino, String nombreEquipoDestino, String codigoEquipoDestino,
 Long idTorneo, String nombreTorneo, String categoria, String mensaje, String motivoResolucion,
 String nombreRevisor, LocalDateTime fechaSolicitud, LocalDateTime fechaResolucion) {}
