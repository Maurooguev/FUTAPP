package com.futapp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jugador")
    private Long idJugador;

    private String nombre;
    private String apellido;
    private String dni;

    @Column(name = "codigo_publico", nullable = false, unique = true, length = 11)
    private String codigoPublico;

    private String apodo;

    @Column(name = "pierna_habil")
    private String piernaHabil;

    @Column(name = "fecha_nacimiento")
    private String fechaNacimiento;

    @Column(name = "numero_camiseta")
    private Integer numeroCamiseta;

    private String posicion;
    private String foto;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    public Jugador() {
    }

    @PrePersist
    void generarCodigoPublico() {
        if (codigoPublico == null || codigoPublico.isBlank()) {
            codigoPublico = "JUG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        }
    }

    public Long getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(Long idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
    public String getApodo() { return apodo; }
    public void setApodo(String apodo) { this.apodo = apodo; }
    public String getPiernaHabil() { return piernaHabil; }
    public void setPiernaHabil(String piernaHabil) { this.piernaHabil = piernaHabil; }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getNumeroCamiseta() {
        return numeroCamiseta;
    }

    public void setNumeroCamiseta(Integer numeroCamiseta) {
        this.numeroCamiseta = numeroCamiseta;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }
}
