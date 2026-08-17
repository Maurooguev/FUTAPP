package com.futapp.dto;

public class FechaResponseDTO {

    private Long idFecha;
    private Integer numeroFecha;
    private String descripcion;
    private String estado;
    private Long idTorneo;
    private String nombreTorneo;

    public FechaResponseDTO() {
    }

    public FechaResponseDTO(Long idFecha, Integer numeroFecha, String descripcion, String estado,
                            Long idTorneo, String nombreTorneo) {
        this.idFecha = idFecha;
        this.numeroFecha = numeroFecha;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idTorneo = idTorneo;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

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
}