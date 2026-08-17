package com.futapp.dto;

public class GolResponseDTO {

    private Long idGol;
    private Integer minuto;
    private String tipoGol;
    private String observacion;

    private Long idPartido;
    private Long idJugador;
    private String nombreJugador;
    private Long idEquipo;
    private String nombreEquipo;

    public GolResponseDTO() {
    }

    public GolResponseDTO(Long idGol, Integer minuto, String tipoGol, String observacion,
                          Long idPartido, Long idJugador, String nombreJugador,
                          Long idEquipo, String nombreEquipo) {
        this.idGol = idGol;
        this.minuto = minuto;
        this.tipoGol = tipoGol;
        this.observacion = observacion;
        this.idPartido = idPartido;
        this.idJugador = idJugador;
        this.nombreJugador = nombreJugador;
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
    }

    public Long getIdGol() {
        return idGol;
    }

    public void setIdGol(Long idGol) {
        this.idGol = idGol;
    }

    public Integer getMinuto() {
        return minuto;
    }

    public void setMinuto(Integer minuto) {
        this.minuto = minuto;
    }

    public String getTipoGol() {
        return tipoGol;
    }

    public void setTipoGol(String tipoGol) {
        this.tipoGol = tipoGol;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Long getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Long idPartido) {
        this.idPartido = idPartido;
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

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }
}