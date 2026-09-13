package com.futapp.dto;
import java.time.LocalDateTime;
public record ResultadoAuditoriaDTO(Long id, Long idPartido, Long idUsuario, String usuario,
        Integer golesLocalAnterior, Integer golesVisitanteAnterior, Integer penalesLocalAnterior, Integer penalesVisitanteAnterior,
        Integer golesLocalNuevo, Integer golesVisitanteNuevo, Integer penalesLocalNuevo, Integer penalesVisitanteNuevo,
        LocalDateTime fechaCambio) {}
