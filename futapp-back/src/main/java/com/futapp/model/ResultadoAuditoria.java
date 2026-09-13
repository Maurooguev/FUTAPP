package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "resultado_auditoria")
public class ResultadoAuditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_auditoria") private Long idAuditoria;
    @ManyToOne(optional = false) @JoinColumn(name = "id_partido") private Partido partido;
    @ManyToOne @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(name = "goles_local_anterior") private Integer golesLocalAnterior;
    @Column(name = "goles_visitante_anterior") private Integer golesVisitanteAnterior;
    @Column(name = "penales_local_anterior") private Integer penalesLocalAnterior;
    @Column(name = "penales_visitante_anterior") private Integer penalesVisitanteAnterior;
    @Column(name = "goles_local_nuevo", nullable = false) private Integer golesLocalNuevo;
    @Column(name = "goles_visitante_nuevo", nullable = false) private Integer golesVisitanteNuevo;
    @Column(name = "penales_local_nuevo") private Integer penalesLocalNuevo;
    @Column(name = "penales_visitante_nuevo") private Integer penalesVisitanteNuevo;
    @Column(name = "fecha_cambio", nullable = false) private LocalDateTime fechaCambio = LocalDateTime.now();
    public Long getIdAuditoria() { return idAuditoria; }
    public Partido getPartido() { return partido; } public void setPartido(Partido value) { partido = value; }
    public Usuario getUsuario() { return usuario; } public void setUsuario(Usuario value) { usuario = value; }
    public Integer getGolesLocalAnterior() { return golesLocalAnterior; } public void setGolesLocalAnterior(Integer value) { golesLocalAnterior = value; }
    public Integer getGolesVisitanteAnterior() { return golesVisitanteAnterior; } public void setGolesVisitanteAnterior(Integer value) { golesVisitanteAnterior = value; }
    public Integer getPenalesLocalAnterior() { return penalesLocalAnterior; } public void setPenalesLocalAnterior(Integer value) { penalesLocalAnterior = value; }
    public Integer getPenalesVisitanteAnterior() { return penalesVisitanteAnterior; } public void setPenalesVisitanteAnterior(Integer value) { penalesVisitanteAnterior = value; }
    public Integer getGolesLocalNuevo() { return golesLocalNuevo; } public void setGolesLocalNuevo(Integer value) { golesLocalNuevo = value; }
    public Integer getGolesVisitanteNuevo() { return golesVisitanteNuevo; } public void setGolesVisitanteNuevo(Integer value) { golesVisitanteNuevo = value; }
    public Integer getPenalesLocalNuevo() { return penalesLocalNuevo; } public void setPenalesLocalNuevo(Integer value) { penalesLocalNuevo = value; }
    public Integer getPenalesVisitanteNuevo() { return penalesVisitanteNuevo; } public void setPenalesVisitanteNuevo(Integer value) { penalesVisitanteNuevo = value; }
    public LocalDateTime getFechaCambio() { return fechaCambio; }
}
