package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizacionInscripcionJugadorDTO(
        @NotNull(message = "El número es obligatorio")
        @Min(value = 1, message = "El número debe ser mayor a cero")
        @Max(value = 99, message = "El número no puede superar 99") Integer numeroCamiseta,
        String posicion
) {}
