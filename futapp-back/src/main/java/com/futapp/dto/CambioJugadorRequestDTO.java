package com.futapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.futapp.validation.ValidBirthDate;

public record CambioJugadorRequestDTO(
        @NotNull Long idTorneo,
        @NotNull Long idEquipo,
        @NotBlank String nombre,
        @NotBlank String apellido,
        String dni,
        @NotBlank
        @ValidBirthDate
        String fechaNacimiento,
        String mensaje) {}
