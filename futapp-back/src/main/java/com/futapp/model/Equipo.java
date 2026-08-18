package com.futapp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    private String nombre;
    private String escudo;

    @Column(name = "codigo_publico", nullable = false, unique = true, length = 10)
    private String codigoPublico;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "foto_plantel", length = 500)
    private String fotoPlantel;

    @Column(name = "email_contacto")
    private String emailContacto;

    @Column(name = "color_camiseta")
    private String colorCamiseta;

    private String barrio;
    private String responsable;

    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;

    public Equipo() {
    }

    @PrePersist
    void generarCodigoPublico() {
        if (codigoPublico == null || codigoPublico.isBlank()) {
            codigoPublico = "EQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        }
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

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }
}
