package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_reclamo")
public class SolicitudReclamo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_solicitud") private Long idSolicitud;
    private String tipo; private String estado = "PENDIENTE";
    @ManyToOne(optional = false) @JoinColumn(name = "id_solicitante") private Usuario solicitante;
    @ManyToOne @JoinColumn(name = "id_usuario_destino") private Usuario usuarioDestino;
    @Column(name = "email_destino") private String emailDestino;
    @ManyToOne @JoinColumn(name = "id_equipo") private Equipo equipo;
    @ManyToOne @JoinColumn(name = "id_equipo_destino") private Equipo equipoDestino;
    @ManyToOne @JoinColumn(name = "id_jugador") private Jugador jugador;
    @ManyToOne(optional = false) @JoinColumn(name = "id_torneo") private Torneo torneo;
    @ManyToOne @JoinColumn(name = "id_revisor") private Usuario revisor;
    private String mensaje; private String categoria; @Column(name = "motivo_resolucion") private String motivoResolucion;
    @Column(name = "nombre_propuesto") private String nombrePropuesto;
    @Column(name = "apellido_propuesto") private String apellidoPropuesto;
    @Column(name = "dni_propuesto") private String dniPropuesto;
    @Column(name = "fecha_nacimiento_propuesta") private String fechaNacimientoPropuesta;
    @Column(name = "fecha_solicitud") private LocalDateTime fechaSolicitud = LocalDateTime.now();
    @Column(name = "fecha_resolucion") private LocalDateTime fechaResolucion;
    public Long getIdSolicitud() { return idSolicitud; } public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
    public Usuario getSolicitante() { return solicitante; } public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }
    public Usuario getUsuarioDestino() { return usuarioDestino; } public void setUsuarioDestino(Usuario value) { usuarioDestino = value; }
    public String getEmailDestino() { return emailDestino; } public void setEmailDestino(String value) { emailDestino = value == null ? null : value.trim().toLowerCase(); }
    public Equipo getEquipo() { return equipo; } public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Equipo getEquipoDestino() { return equipoDestino; } public void setEquipoDestino(Equipo value) { this.equipoDestino = value; }
    public Jugador getJugador() { return jugador; } public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public Torneo getTorneo() { return torneo; } public void setTorneo(Torneo torneo) { this.torneo = torneo; }
    public Usuario getRevisor() { return revisor; } public void setRevisor(Usuario revisor) { this.revisor = revisor; }
    public String getMensaje() { return mensaje; } public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getCategoria() { return categoria; } public void setCategoria(String categoria) { this.categoria = TextNormalizer.titleCase(categoria); }
    public String getMotivoResolucion() { return motivoResolucion; } public void setMotivoResolucion(String value) { this.motivoResolucion = value; }
    public String getNombrePropuesto() { return nombrePropuesto; } public void setNombrePropuesto(String value) { nombrePropuesto = TextNormalizer.titleCase(value); }
    public String getApellidoPropuesto() { return apellidoPropuesto; } public void setApellidoPropuesto(String value) { apellidoPropuesto = TextNormalizer.titleCase(value); }
    public String getDniPropuesto() { return dniPropuesto; } public void setDniPropuesto(String value) { dniPropuesto = value; }
    public String getFechaNacimientoPropuesta() { return fechaNacimientoPropuesta; } public void setFechaNacimientoPropuesta(String value) { fechaNacimientoPropuesta = value; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; } public LocalDateTime getFechaResolucion() { return fechaResolucion; } public void setFechaResolucion(LocalDateTime value) { this.fechaResolucion = value; }
}
