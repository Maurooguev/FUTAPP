package com.futapp.dto;

import java.util.List;

public record EstructuraTorneoDTO(Long idTorneo, List<Division> divisiones) {
    public record Division(Long id, String nombre, Integer orden, List<Fase> fases, List<Cupo> cupos) {}
    public record Fase(Long id, String nombre, String tipo, Integer orden, Integer clasificados,
                       String estado, List<Zona> zonas) {}
    public record Zona(Long id, String nombre, Integer orden) {}
    public record Cupo(Long id, Integer numero, String etiqueta, Long idZona, String zona,
                       Long idEquipo, String equipo) {}
}
