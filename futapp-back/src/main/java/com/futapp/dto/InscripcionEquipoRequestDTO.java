package com.futapp.dto;

import jakarta.validation.constraints.NotNull;

public record InscripcionEquipoRequestDTO(
        @NotNull(message = "El equipo es obligatorio") Long idEquipo,
        @NotNull(message = "El torneo es obligatorio") Long idTorneo,
        String categoria
) {}
