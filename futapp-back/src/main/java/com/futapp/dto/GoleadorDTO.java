package com.futapp.dto;

public class GoleadorDTO {

    private Long idJugador;
    private String nombreJugador;
    private String nombreEquipo;
    private Long cantidadGoles;

    public GoleadorDTO(Long idJugador, String nombreJugador, String nombreEquipo, Long cantidadGoles) {
        this.idJugador = idJugador;
        this.nombreJugador = nombreJugador;
        this.nombreEquipo = nombreEquipo;
        this.cantidadGoles = cantidadGoles;
    }

    public Long getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(Long idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }

    public Long getCantidadGoles() {
        return cantidadGoles;
    }

    public void setCantidadGoles(Long cantidadGoles) {
        this.cantidadGoles = cantidadGoles;
    }
}