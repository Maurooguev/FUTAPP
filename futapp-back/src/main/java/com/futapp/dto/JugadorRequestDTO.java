package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JugadorRequestDTO {

    @NotBlank(message = "El nombre del jugador es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido del jugador es obligatorio")
    private String apellido;
    private String dni;
    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    private String fechaNacimiento;
    @NotNull(message = "El número de camiseta es obligatorio")
    @Min(value = 1, message = "El número de camiseta debe ser mayor a cero")
    @Max(value = 99, message = "El número de camiseta no puede superar 99")
    private Integer numeroCamiseta;
    @NotBlank(message = "La posición es obligatoria")
    private String posicion;
    private String foto;
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    @NotNull(message = "El equipo es obligatorio")
    private Long idEquipo;

    public JugadorRequestDTO() {
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

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }
}
