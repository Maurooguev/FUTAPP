package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;

public record BajaEquipoTorneoRequestDTO(
        @NotBlank String resolucionPartidos,
        String motivo
) {}
