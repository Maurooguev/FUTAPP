package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partido")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partido")
    private Long idPartido;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    private String estado;

    @Column(name = "goles_local")
    private Integer golesLocal = 0;

    @Column(name = "goles_visitante")
    private Integer golesVisitante = 0;
    @Column(name = "penales_local") private Integer penalesLocal;
    @Column(name = "penales_visitante") private Integer penalesVisitante;

    private String observaciones;

    @Column(length = 150)
    private String cancha;

    @ManyToOne
    @JoinColumn(name = "id_mvp")
    private Jugador mvp;

    @ManyToOne
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "id_fecha", nullable = false)
    private Fecha fecha;

    @ManyToOne
    @JoinColumn(name = "id_equipo_local", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne
    @JoinColumn(name = "id_equipo_visitante", nullable = false)
    private Equipo equipoVisitante;

    public Partido() {
    }

    public Long getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Long idPartido) {
        this.idPartido = idPartido;
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
    public Integer getPenalesLocal() { return penalesLocal; }
    public void setPenalesLocal(Integer value) { this.penalesLocal = value; }
    public Integer getPenalesVisitante() { return penalesVisitante; }
    public void setPenalesVisitante(Integer value) { this.penalesVisitante = value; }

    public String getCancha() { return cancha; }
    public void setCancha(String cancha) { this.cancha = cancha; }
    public Jugador getMvp() { return mvp; }
    public void setMvp(Jugador mvp) { this.mvp = mvp; }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Fecha getFecha() {
        return fecha;
    }

    public void setFecha(Fecha fecha) {
        this.fecha = fecha;
    }

    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(Equipo equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(Equipo equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }
}
