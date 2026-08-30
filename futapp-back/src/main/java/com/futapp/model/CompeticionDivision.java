package com.futapp.model;

import jakarta.persistence.*;

@Entity @Table(name = "competicion_division")
public class CompeticionDivision {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_division") private Long idDivision;
    @ManyToOne(optional = false) @JoinColumn(name = "id_torneo") private Torneo torneo;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false) private Integer orden;
    public Long getIdDivision() { return idDivision; }
    public Torneo getTorneo() { return torneo; } public void setTorneo(Torneo torneo) { this.torneo = torneo; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getOrden() { return orden; } public void setOrden(Integer orden) { this.orden = orden; }
}
