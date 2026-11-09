package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GolRequestDTO {

    private Integer minuto;
    @NotBlank(message = "El tipo de gol es obligatorio")
    private String tipoGol;
    private String observacion;
    @NotNull(message = "El partido es obligatorio")
    private Long idPartido;
    @NotNull(message = "El jugador es obligatorio")
    private Long idJugador;
    @NotNull(message = "El equipo es obligatorio")
    private Long idEquipo;

    public GolRequestDTO() {
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

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }
}
