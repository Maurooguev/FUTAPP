package com.futapp.dto;

import java.time.LocalDate;

public record InscripcionEquipoResponseDTO(
        Long idInscripcion,
        Long idEquipo,
        String nombreEquipo,
        String codigoEquipo,
        Long idTorneo,
        String nombreTorneo,
        String codigoTorneo,
        String categoria,
        String estado,
        LocalDate fechaAlta,
        LocalDate fechaBaja,
        long cantidadJugadores
) {}
