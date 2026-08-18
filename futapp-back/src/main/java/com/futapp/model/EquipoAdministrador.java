package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipo_administrador", uniqueConstraints = @UniqueConstraint(columnNames = {"id_equipo", "id_usuario"}))
public class EquipoAdministrador {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo_administrador") private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_equipo") private Equipo equipo;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @ManyToOne @JoinColumn(name = "id_aprobado_por") private Usuario aprobadoPor;
    private String estado = "ACTIVO";
    @Column(name = "fecha_alta") private LocalDateTime fechaAlta = LocalDateTime.now();
    public Long getId() { return id; } public Equipo getEquipo() { return equipo; } public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Usuario getUsuario() { return usuario; } public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getAprobadoPor() { return aprobadoPor; } public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }
    public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
}
