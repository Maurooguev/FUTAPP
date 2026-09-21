package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_reclamo")
public class SolicitudReclamo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_solicitud") private Long idSolicitud;
    private String tipo; private String estado = "PENDIENTE";
    @ManyToOne(optional = false) @JoinColumn(name = "id_solicitante") private Usuario solicitante;
    @ManyToOne @JoinColumn(name = "id_equipo") private Equipo equipo;
    @ManyToOne @JoinColumn(name = "id_equipo_destino") private Equipo equipoDestino;
    @ManyToOne @JoinColumn(name = "id_jugador") private Jugador jugador;
    @ManyToOne(optional = false) @JoinColumn(name = "id_torneo") private Torneo torneo;
    @ManyToOne @JoinColumn(name = "id_revisor") private Usuario revisor;
    private String mensaje; private String categoria; @Column(name = "motivo_resolucion") private String motivoResolucion;
    @Column(name = "fecha_solicitud") private LocalDateTime fechaSolicitud = LocalDateTime.now();
    @Column(name = "fecha_resolucion") private LocalDateTime fechaResolucion;
    public Long getIdSolicitud() { return idSolicitud; } public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
    public Usuario getSolicitante() { return solicitante; } public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }
    public Equipo getEquipo() { return equipo; } public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Equipo getEquipoDestino() { return equipoDestino; } public void setEquipoDestino(Equipo value) { this.equipoDestino = value; }
    public Jugador getJugador() { return jugador; } public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public Torneo getTorneo() { return torneo; } public void setTorneo(Torneo torneo) { this.torneo = torneo; }
    public Usuario getRevisor() { return revisor; } public void setRevisor(Usuario revisor) { this.revisor = revisor; }
    public String getMensaje() { return mensaje; } public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getCategoria() { return categoria; } public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getMotivoResolucion() { return motivoResolucion; } public void setMotivoResolucion(String value) { this.motivoResolucion = value; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; } public LocalDateTime getFechaResolucion() { return fechaResolucion; } public void setFechaResolucion(LocalDateTime value) { this.fechaResolucion = value; }
}
