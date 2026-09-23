package com.futapp.dto;

import java.time.LocalDateTime;

public record PartidoExplorarDTO(
        Long idPartido,
        LocalDateTime fechaHora,
        String estado,
        Integer golesLocal,
        Integer golesVisitante,
        Long idTorneo,
        String nombreTorneo,
        Long idEquipoLocal,
        String nombreEquipoLocal,
        Long idEquipoVisitante,
        String nombreEquipoVisitante
) {
}
