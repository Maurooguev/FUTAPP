package com.futapp.model;

import jakarta.persistence.*;

@Entity @Table(name = "cupo_equipo")
public class CupoEquipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_cupo") private Long idCupo;
    @ManyToOne(optional = false) @JoinColumn(name = "id_division") private CompeticionDivision division;
    @ManyToOne @JoinColumn(name = "id_zona") private CompeticionZona zona;
    @ManyToOne @JoinColumn(name = "id_equipo") private Equipo equipo;
    @Column(nullable = false) private Integer numero;
    @Column(nullable = false, length = 80) private String etiqueta;
    public Long getIdCupo() { return idCupo; }
    public CompeticionDivision getDivision() { return division; } public void setDivision(CompeticionDivision division) { this.division = division; }
    public CompeticionZona getZona() { return zona; } public void setZona(CompeticionZona zona) { this.zona = zona; }
    public Equipo getEquipo() { return equipo; } public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Integer getNumero() { return numero; } public void setNumero(Integer numero) { this.numero = numero; }
    public String getEtiqueta() { return etiqueta; } public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
}
