package com.futapp.dto;
import java.time.LocalDateTime;
public record NotificacionResponseDTO(Long idNotificacion, String tipo, String titulo, String mensaje,
                                      String url, boolean leida, LocalDateTime fechaCreacion) {}
