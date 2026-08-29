package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record FixtureRequestDTO(
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
        @NotNull(message = "El horario inicial es obligatorio") LocalTime horaInicio,
        @Min(value = 1, message = "La separación entre jornadas debe ser de al menos 1 día")
        @Max(value = 60, message = "La separación entre jornadas no puede superar 60 días") Integer diasEntreJornadas,
        @Min(value = 0, message = "La separación entre partidos no puede ser negativa")
        @Max(value = 720, message = "La separación entre partidos no puede superar 720 minutos") Integer minutosEntrePartidos
) {}
