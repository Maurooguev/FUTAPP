package com.futapp.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
public record PerfilJugadorPersonalDTO(String foto, String apodo, String fechaNacimiento,
 String piernaHabil, String equipoFavorito, String barrio,
 @Min(100) @Max(250) Integer alturaCm, String nacionalidad, String biografia, String instagram) {}
