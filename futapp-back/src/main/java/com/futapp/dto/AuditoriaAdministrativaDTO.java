package com.futapp.dto;

import java.time.LocalDateTime;

public record AuditoriaAdministrativaDTO(Long id, Long idUsuario, String usuario, String metodo,
                                         String ruta, int estadoHttp, String resultado,
                                         String requestId, LocalDateTime fecha) {}
