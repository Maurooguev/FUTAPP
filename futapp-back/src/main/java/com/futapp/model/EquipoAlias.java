package com.futapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo_alias", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class EquipoAlias {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alias") private Long idAlias;
    @Column(nullable = false, length = 10) private String codigo;
    @ManyToOne(optional = false) @JoinColumn(name = "id_equipo") private Equipo equipo;

    public Long getIdAlias() { return idAlias; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
}
