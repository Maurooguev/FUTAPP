package com.futapp.dto;

public class GoleadorDTO {

    private Long idJugador;
    private String nombreJugador;
    private String fotoJugador;
    private Long idEquipo;
    private String nombreEquipo;
    private Long cantidadGoles;

    public GoleadorDTO(Long idJugador, String nombreJugador, String fotoJugador, Long idEquipo, String nombreEquipo, Long cantidadGoles) {
        this.idJugador = idJugador;
        this.nombreJugador = nombreJugador;
        this.fotoJugador = fotoJugador;
        this.idEquipo = idEquipo;
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

    public String getFotoJugador() { return fotoJugador; }

    public void setFotoJugador(String fotoJugador) { this.fotoJugador = fotoJugador; }

    public Long getIdEquipo() { return idEquipo; }

    public void setIdEquipo(Long idEquipo) { this.idEquipo = idEquipo; }

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
