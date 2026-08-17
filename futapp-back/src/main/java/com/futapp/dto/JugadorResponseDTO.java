package com.futapp.dto;

public class JugadorResponseDTO {

    private Long idJugador;
    private String nombre;
    private String apellido;
    private String dni;
    private String fechaNacimiento;
    private Integer numeroCamiseta;
    private String posicion;
    private String foto;
    private String estado;
    private Long idEquipo;
    private String nombreEquipo;

    public JugadorResponseDTO() {
    }

    public JugadorResponseDTO(Long idJugador, String nombre, String apellido, String dni,
                              String fechaNacimiento, Integer numeroCamiseta, String posicion,
                              String foto, String estado, Long idEquipo, String nombreEquipo) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.numeroCamiseta = numeroCamiseta;
        this.posicion = posicion;
        this.foto = foto;
        this.estado = estado;
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
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

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }
}