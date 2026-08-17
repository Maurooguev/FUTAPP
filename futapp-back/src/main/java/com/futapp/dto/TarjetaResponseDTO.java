package com.futapp.dto;

public class TarjetaResponseDTO {

    private Long idTarjeta;
    private String tipoTarjeta;
    private Integer minuto;
    private String motivo;

    private Long idPartido;
    private Long idJugador;
    private String nombreJugador;
    private Long idEquipo;
    private String nombreEquipo;

    public TarjetaResponseDTO() {
    }

    public TarjetaResponseDTO(Long idTarjeta, String tipoTarjeta, Integer minuto, String motivo,
                              Long idPartido, Long idJugador, String nombreJugador,
                              Long idEquipo, String nombreEquipo) {
        this.idTarjeta = idTarjeta;
        this.tipoTarjeta = tipoTarjeta;
        this.minuto = minuto;
        this.motivo = motivo;
        this.idPartido = idPartido;
        this.idJugador = idJugador;
        this.nombreJugador = nombreJugador;
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
    }

    public Long getIdTarjeta() {
        return idTarjeta;
    }

    public void setIdTarjeta(Long idTarjeta) {
        this.idTarjeta = idTarjeta;
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