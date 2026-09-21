package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;

@Entity @Table(name = "competicion_zona")
public class CompeticionZona {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_zona") private Long idZona;
    @ManyToOne(optional = false) @JoinColumn(name = "id_fase") private CompeticionFase fase;
    @Column(nullable = false, length = 60) private String nombre;
    @Column(nullable = false) private Integer orden;
    public Long getIdZona() { return idZona; }
    public CompeticionFase getFase() { return fase; } public void setFase(CompeticionFase fase) { this.fase = fase; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = TextNormalizer.titleCase(nombre); }
    public Integer getOrden() { return orden; } public void setOrden(Integer orden) { this.orden = orden; }
}
