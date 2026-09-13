package com.futapp.dto;

public class PartidoResponseDTO {

    private Long idPartido;
    private String fechaHora;
    private String estado;
    private Integer golesLocal;
    private Integer golesVisitante;
    private Integer penalesLocal;
    private Integer penalesVisitante;
    private String observaciones;
    private String cancha;
    private Long idMvp;
    private String nombreMvp;
    private String fotoMvp;

    private Long idTorneo;
    private String nombreTorneo;

    private Long idFecha;
    private Integer numeroFecha;

    private Long idEquipoLocal;
    private String nombreEquipoLocal;

    private Long idEquipoVisitante;
    private String nombreEquipoVisitante;

    public PartidoResponseDTO() {
    }

    public PartidoResponseDTO(Long idPartido, String fechaHora, String estado,
                              Integer golesLocal, Integer golesVisitante, String observaciones, String cancha,
                              Long idMvp, String nombreMvp, String fotoMvp,
                              Long idTorneo, String nombreTorneo,
                              Long idFecha, Integer numeroFecha,
                              Long idEquipoLocal, String nombreEquipoLocal,
                              Long idEquipoVisitante, String nombreEquipoVisitante) {
        this.idPartido = idPartido;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.observaciones = observaciones;
        this.cancha = cancha;
        this.idMvp = idMvp;
        this.nombreMvp = nombreMvp;
        this.fotoMvp = fotoMvp;
        this.idTorneo = idTorneo;
        this.nombreTorneo = nombreTorneo;
        this.idFecha = idFecha;
        this.numeroFecha = numeroFecha;
        this.idEquipoLocal = idEquipoLocal;
        this.nombreEquipoLocal = nombreEquipoLocal;
        this.idEquipoVisitante = idEquipoVisitante;
        this.nombreEquipoVisitante = nombreEquipoVisitante;
    }

    public Long getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Long idPartido) {
        this.idPartido = idPartido;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    public Integer getPenalesLocal() { return penalesLocal; }
    public void setPenalesLocal(Integer value) { this.penalesLocal = value; }
    public Integer getPenalesVisitante() { return penalesVisitante; }
    public void setPenalesVisitante(Integer value) { this.penalesVisitante = value; }

    public String getCancha() { return cancha; }
    public void setCancha(String cancha) { this.cancha = cancha; }
    public Long getIdMvp() { return idMvp; }
    public void setIdMvp(Long idMvp) { this.idMvp = idMvp; }
    public String getNombreMvp() { return nombreMvp; }
    public void setNombreMvp(String nombreMvp) { this.nombreMvp = nombreMvp; }
    public String getFotoMvp() { return fotoMvp; }
    public void setFotoMvp(String fotoMvp) { this.fotoMvp = fotoMvp; }

    public Long getIdTorneo() {
        return idTorneo;
    }

    public void setIdTorneo(Long idTorneo) {
        this.idTorneo = idTorneo;
    }

    public String getNombreTorneo() {
        return nombreTorneo;
    }

    public void setNombreTorneo(String nombreTorneo) {
        this.nombreTorneo = nombreTorneo;
    }

    public Long getIdFecha() {
        return idFecha;
    }

    public void setIdFecha(Long idFecha) {
        this.idFecha = idFecha;
    }

    public Integer getNumeroFecha() {
        return numeroFecha;
    }

    public void setNumeroFecha(Integer numeroFecha) {
        this.numeroFecha = numeroFecha;
    }

    public Long getIdEquipoLocal() {
        return idEquipoLocal;
    }

    public void setIdEquipoLocal(Long idEquipoLocal) {
        this.idEquipoLocal = idEquipoLocal;
    }

    public String getNombreEquipoLocal() {
        return nombreEquipoLocal;
    }

    public void setNombreEquipoLocal(String nombreEquipoLocal) {
        this.nombreEquipoLocal = nombreEquipoLocal;
    }

    public Long getIdEquipoVisitante() {
        return idEquipoVisitante;
    }

    public void setIdEquipoVisitante(Long idEquipoVisitante) {
        this.idEquipoVisitante = idEquipoVisitante;
    }

    public String getNombreEquipoVisitante() {
        return nombreEquipoVisitante;
    }

    public void setNombreEquipoVisitante(String nombreEquipoVisitante) {
        this.nombreEquipoVisitante = nombreEquipoVisitante;
    }
}
