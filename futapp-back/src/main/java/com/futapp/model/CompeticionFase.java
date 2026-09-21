package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;

@Entity @Table(name = "competicion_fase")
public class CompeticionFase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_fase") private Long idFase;
    @ManyToOne(optional = false) @JoinColumn(name = "id_division") private CompeticionDivision division;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 30) private String tipo;
    @Column(nullable = false) private Integer orden;
    private Integer clasificados;
    @Column(nullable = false, length = 20) private String estado = "PENDIENTE";
    public Long getIdFase() { return idFase; }
    public CompeticionDivision getDivision() { return division; } public void setDivision(CompeticionDivision division) { this.division = division; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = TextNormalizer.titleCase(nombre); }
    public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getOrden() { return orden; } public void setOrden(Integer orden) { this.orden = orden; }
    public Integer getClasificados() { return clasificados; } public void setClasificados(Integer clasificados) { this.clasificados = clasificados; }
    public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
}
