package com.futapp.dto;

public record SancionResponseDTO(
        Long idSancion,
        Long idJugador,
        String nombreJugador,
        String fotoJugador,
        Long idEquipo,
        String nombreEquipo,
        Integer fechasTotal,
        Integer fechasCumplidas,
        Integer fechasRestantes,
        String motivo,
        Integer fechaOrigen,
        boolean activa
) {}

