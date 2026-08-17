package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TarjetaRequestDTO {

    @NotBlank(message = "El tipo de tarjeta es obligatorio")
    private String tipoTarjeta;
    @Min(value = 1, message = "El minuto debe ser mayor a cero")
    @Max(value = 130, message = "El minuto no puede superar 130")
    private Integer minuto;
    private String motivo;
    @NotNull(message = "El partido es obligatorio")
    private Long idPartido;
    @NotNull(message = "El jugador es obligatorio")
    private Long idJugador;

    public TarjetaRequestDTO() {
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public Integer getMinuto() {
        return minuto;
    }

    public void setMinuto(Integer minuto) {
        this.minuto = minuto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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
}
