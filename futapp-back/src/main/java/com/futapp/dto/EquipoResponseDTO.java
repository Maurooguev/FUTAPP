package com.futapp.dto;

public class EquipoResponseDTO {

    private Long idEquipo;
    private String nombre;
    private String escudo;
    private String codigoPublico;
    private String descripcion;
    private String fotoPlantel;
    private String emailContacto;
    private String colorCamiseta;
    private String barrio;
    private String responsable;
    private String telefonoContacto;
    private String estado;
    private Long idTorneo;
    private String nombreTorneo;

    public EquipoResponseDTO() {
    }

    public EquipoResponseDTO(Long idEquipo, String nombre, String escudo, String codigoPublico,
                             String descripcion, String fotoPlantel, String emailContacto, String colorCamiseta,
                             String barrio, String responsable, String telefonoContacto,
                             String estado, Long idTorneo, String nombreTorneo) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.escudo = escudo;
        this.codigoPublico = codigoPublico;
        this.descripcion = descripcion;
        this.fotoPlantel = fotoPlantel;
        this.emailContacto = emailContacto;
        this.colorCamiseta = colorCamiseta;
        this.barrio = barrio;
        this.responsable = responsable;
        this.telefonoContacto = telefonoContacto;
        this.estado = estado;
        this.idTorneo = idTorneo;
        this.nombreTorneo = nombreTorneo;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEscudo() {
        return escudo;
    }

    public void setEscudo(String escudo) {
        this.escudo = escudo;
    }

    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFotoPlantel() { return fotoPlantel; }
    public void setFotoPlantel(String fotoPlantel) { this.fotoPlantel = fotoPlantel; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public String getColorCamiseta() {
        return colorCamiseta;
    }

    public void setColorCamiseta(String colorCamiseta) {
        this.colorCamiseta = colorCamiseta;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
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
