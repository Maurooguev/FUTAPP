package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EquipoRequestDTO {

    @NotBlank(message = "El nombre del equipo es obligatorio")
    @Size(max = 120, message = "El nombre del equipo no puede superar los 120 caracteres")
    private String nombre;
    private String escudo;
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
    private String fotoPlantel;
    private String emailContacto;
    @NotBlank(message = "El color de camiseta es obligatorio")
    private String colorCamiseta;
    @NotBlank(message = "El barrio es obligatorio")
    private String barrio;
    @NotBlank(message = "El responsable es obligatorio")
    private String responsable;
    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefonoContacto;
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    @NotNull(message = "El torneo es obligatorio")
    private Long idTorneo;

    public EquipoRequestDTO() {
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
}
