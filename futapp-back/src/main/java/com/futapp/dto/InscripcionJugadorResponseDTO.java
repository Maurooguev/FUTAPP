package com.futapp.dto;

import java.time.LocalDate;

public record InscripcionJugadorResponseDTO(
        Long idInscripcionJugador,
        Long idInscripcionEquipo,
        Long idJugador,
        String nombreJugador,
        String codigoJugador,
        String foto,
        Integer numeroCamiseta,
        String posicion,
        String estado,
        LocalDate fechaAlta,
        LocalDate fechaBaja,
        Long idEquipo,
        String nombreEquipo,
        Long idTorneo,
        String nombreTorneo
) {}
