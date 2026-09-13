package com.futapp.dto;

public class ResultadoPartidoDTO {

    private Integer golesLocal;
    private Integer golesVisitante;
    private Integer penalesLocal;
    private Integer penalesVisitante;

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }
    public Integer getPenalesLocal() { return penalesLocal; }
    public void setPenalesLocal(Integer value) { this.penalesLocal = value; }
    public Integer getPenalesVisitante() { return penalesVisitante; }
    public void setPenalesVisitante(Integer value) { this.penalesVisitante = value; }
}
