package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PartidoRequestDTO {

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    @PositiveOrZero(message = "Los goles del equipo local no pueden ser negativos")
    private Integer golesLocal;

    @PositiveOrZero(message = "Los goles del equipo visitante no pueden ser negativos")
    private Integer golesVisitante;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    @Size(max = 150, message = "La cancha no puede superar los 150 caracteres")
    private String cancha;

    @NotNull(message = "El torneo es obligatorio")
    private Long idTorneo;

    @NotNull(message = "La jornada es obligatoria")
    private Long idFecha;

    @NotNull(message = "El equipo local es obligatorio")
    private Long idEquipoLocal;

    @NotNull(message = "El equipo visitante es obligatorio")
    private Long idEquipoVisitante;

    public PartidoRequestDTO() {
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
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

    public String getCancha() { return cancha; }
    public void setCancha(String cancha) { this.cancha = cancha; }

    public Long getIdTorneo() {
        return idTorneo;
    }

    public void setIdTorneo(Long idTorneo) {
        this.idTorneo = idTorneo;
    }

    public Long getIdFecha() {
        return idFecha;
    }

    public void setIdFecha(Long idFecha) {
        this.idFecha = idFecha;
    }

    public Long getIdEquipoLocal() {
        return idEquipoLocal;
    }

    public void setIdEquipoLocal(Long idEquipoLocal) {
        this.idEquipoLocal = idEquipoLocal;
    }

    public Long getIdEquipoVisitante() {
        return idEquipoVisitante;
    }

    public void setIdEquipoVisitante(Long idEquipoVisitante) {
        this.idEquipoVisitante = idEquipoVisitante;
    }
}
