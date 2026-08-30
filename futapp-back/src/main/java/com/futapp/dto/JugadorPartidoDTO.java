package com.futapp.dto;

public record JugadorPartidoDTO(
        Long idJugador,
        String nombre,
        String apellido,
        String apodo,
        String foto,
        Long idEquipo,
        String nombreEquipo,
        Integer numeroCamiseta
) {}
