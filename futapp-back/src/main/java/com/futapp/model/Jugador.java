package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;
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

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @Column(name = "equipo_favorito") private String equipoFavorito;
    private String barrio;
    @Column(name = "altura_cm") private Integer alturaCm;
    private String nacionalidad;
    @Column(length = 1000) private String biografia;
    private String instagram;
    @Column(nullable = false) private Long visitas = 0L;

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
        this.nombre = TextNormalizer.titleCase(nombre);
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = TextNormalizer.titleCase(apellido);
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
    public void setApodo(String apodo) { this.apodo = TextNormalizer.titleCase(apodo); }
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
        this.posicion = TextNormalizer.titleCase(posicion);
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

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEquipoFavorito() { return equipoFavorito; }
    public void setEquipoFavorito(String equipoFavorito) { this.equipoFavorito = TextNormalizer.titleCase(equipoFavorito); }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = TextNormalizer.titleCase(barrio); }
    public Integer getAlturaCm() { return alturaCm; }
    public void setAlturaCm(Integer alturaCm) { this.alturaCm = alturaCm; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = TextNormalizer.titleCase(nacionalidad); }
    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }
    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public Long getVisitas() { return visitas; }
    public void setVisitas(Long visitas) { this.visitas = visitas; }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }
}
