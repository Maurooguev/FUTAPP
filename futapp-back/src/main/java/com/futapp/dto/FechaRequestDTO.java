package com.futapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FechaRequestDTO {

    @NotNull(message = "El número de jornada es obligatorio")
    @Min(value = 1, message = "El número de jornada debe ser mayor a cero")
    private Integer numeroFecha;
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    @NotNull(message = "El torneo es obligatorio")
    private Long idTorneo;

    public FechaRequestDTO() {
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
}
