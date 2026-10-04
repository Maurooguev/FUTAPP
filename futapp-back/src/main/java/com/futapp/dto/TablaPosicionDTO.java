package com.futapp.dto;

public class TablaPosicionDTO {

    private Long idEquipo;
    private String nombreEquipo;
    private String escudoEquipo;
    private int puntos;
    private int partidosJugados;
    private int ganados;
    private int empatados;
    private int perdidos;
    private int golesFavor;
    private int golesContra;
    private int diferenciaGol;

    public TablaPosicionDTO() {
    }

    public TablaPosicionDTO(Long idEquipo, String nombreEquipo, String escudoEquipo) {
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
        this.escudoEquipo = escudoEquipo;
        this.puntos = 0;
        this.partidosJugados = 0;
        this.ganados = 0;
        this.empatados = 0;
        this.perdidos = 0;
        this.golesFavor = 0;
        this.golesContra = 0;
        this.diferenciaGol = 0;
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

    public String getEscudoEquipo() {
        return escudoEquipo;
    }

    public void setEscudoEquipo(String escudoEquipo) {
        this.escudoEquipo = escudoEquipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(int partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public int getGanados() {
        return ganados;
    }

    public void setGanados(int ganados) {
        this.ganados = ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public void setEmpatados(int empatados) {
        this.empatados = empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public void setPerdidos(int perdidos) {
        this.perdidos = perdidos;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public void setGolesFavor(int golesFavor) {
        this.golesFavor = golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public void setGolesContra(int golesContra) {
        this.golesContra = golesContra;
    }

    public int getDiferenciaGol() {
        return diferenciaGol;
    }

    public void setDiferenciaGol(int diferenciaGol) {
        this.diferenciaGol = diferenciaGol;
    }
}
