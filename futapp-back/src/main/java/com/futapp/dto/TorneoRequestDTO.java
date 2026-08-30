package com.futapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TorneoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 120, message = "La ubicación no puede superar los 120 caracteres")
    private String ubicacion;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    @NotBlank(message = "El formato es obligatorio")
    private String formato;

    @NotNull(message = "La cantidad de equipos es obligatoria")
    @Min(value = 2, message = "El torneo debe tener al menos 2 equipos")
    @Max(value = 100, message = "El torneo no puede superar los 100 equipos")
    private Integer cantidadEquipos;
    private Boolean idaVuelta = false;
    @Min(0) @Max(10) private Integer puntosVictoria = 3;
    @Min(0) @Max(10) private Integer puntosEmpate = 1;
    @Min(0) @Max(10) private Integer puntosDerrota = 0;
    private String criterioDesempate = "DIFERENCIA_GOLES";
    @Min(value = 2, message = "Debe haber al menos 2 zonas")
    @Max(value = 16, message = "No puede haber más de 16 zonas")
    private Integer cantidadZonas = 2;
    @Min(value = 1, message = "Debe clasificar al menos 1 equipo por zona")
    @Max(value = 16, message = "No pueden clasificar más de 16 equipos por zona")
    private Integer clasificadosPorZona = 2;

    public TorneoRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Integer getCantidadEquipos() {
        return cantidadEquipos;
    }

    public void setCantidadEquipos(Integer cantidadEquipos) {
        this.cantidadEquipos = cantidadEquipos;
    }
    public Boolean getIdaVuelta() { return idaVuelta; } public void setIdaVuelta(Boolean value) { this.idaVuelta = value; }
    public Integer getPuntosVictoria() { return puntosVictoria; } public void setPuntosVictoria(Integer value) { this.puntosVictoria = value; }
    public Integer getPuntosEmpate() { return puntosEmpate; } public void setPuntosEmpate(Integer value) { this.puntosEmpate = value; }
    public Integer getPuntosDerrota() { return puntosDerrota; } public void setPuntosDerrota(Integer value) { this.puntosDerrota = value; }
    public String getCriterioDesempate() { return criterioDesempate; } public void setCriterioDesempate(String value) { this.criterioDesempate = value; }
    public Integer getCantidadZonas() { return cantidadZonas; } public void setCantidadZonas(Integer value) { this.cantidadZonas = value; }
    public Integer getClasificadosPorZona() { return clasificadosPorZona; } public void setClasificadosPorZona(Integer value) { this.clasificadosPorZona = value; }
}
