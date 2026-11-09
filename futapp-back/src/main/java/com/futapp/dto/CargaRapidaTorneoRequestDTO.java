package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CargaRapidaTorneoRequestDTO(
        @NotEmpty(message = "Ingresá al menos un equipo")
        @Size(max = 100, message = "No podés cargar más de 100 equipos por vez")
        List<@Size(max = 120, message = "Cada nombre puede tener hasta 120 caracteres") String> nombresEquipos,
        @Min(value = 1, message = "Cada plantel debe tener al menos un jugador")
        @Max(value = 30, message = "Cada plantel puede tener como máximo 30 jugadores")
        int jugadoresPorEquipo) {}
