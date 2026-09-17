package com.futapp.dto;

public class JugadorResponseDTO {

    private Long idJugador;
    private String nombre;
    private String apellido;
    private String dni;
    private String codigoPublico;
    private String apodo;
    private String piernaHabil;
    private String fechaNacimiento;
    private Integer numeroCamiseta;
    private String posicion;
    private String foto;
    private String estado;
    private Long idEquipo;
    private String nombreEquipo;
    private boolean verificado;
    private Long visitas;

    public JugadorResponseDTO() {
    }

    public JugadorResponseDTO(Long idJugador, String nombre, String apellido, String dni,
                              String codigoPublico, String apodo, String piernaHabil,
                              String fechaNacimiento, Integer numeroCamiseta, String posicion,
                              String foto, String estado, Long idEquipo, String nombreEquipo) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.codigoPublico = codigoPublico;
        this.apodo = apodo;
        this.piernaHabil = piernaHabil;
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

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }
    public Long getVisitas() { return visitas; }
    public void setVisitas(Long visitas) { this.visitas = visitas; }
}
