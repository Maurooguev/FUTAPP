package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadoPartidoRequestDTO(
        @NotBlank(message = "El estado es obligatorio") String estado,
        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres") String motivo
) {}
