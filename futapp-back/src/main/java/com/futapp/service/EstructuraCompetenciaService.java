package com.futapp.service;

import com.futapp.dto.EstructuraTorneoDTO;
import com.futapp.dto.TorneoRequestDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.*;
import com.futapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EstructuraCompetenciaService {
    private final TorneoRepository torneos;
    private final CompeticionDivisionRepository divisiones;
    private final CompeticionFaseRepository fases;
    private final CompeticionZonaRepository zonas;
    private final CupoEquipoRepository cupos;
    private final EquipoRepository equipos;
    private final InscripcionEquipoRepository inscripciones;
    private final JugadorRepository jugadores;
    private final InscripcionJugadorRepository planteles;

    public EstructuraCompetenciaService(TorneoRepository torneos, CompeticionDivisionRepository divisiones,
            CompeticionFaseRepository fases, CompeticionZonaRepository zonas, CupoEquipoRepository cupos,
            EquipoRepository equipos, InscripcionEquipoRepository inscripciones,
            JugadorRepository jugadores, InscripcionJugadorRepository planteles) {
        this.torneos = torneos; this.divisiones = divisiones; this.fases = fases; this.zonas = zonas; this.cupos = cupos;
        this.equipos = equipos; this.inscripciones = inscripciones;
        this.jugadores = jugadores; this.planteles = planteles;
    }

    @Transactional
    public void crear(Torneo torneo, TorneoRequestDTO request) {
        CompeticionDivision division = new CompeticionDivision();
        division.setTorneo(torneo); division.setNombre("División única"); division.setOrden(1);
        division = divisiones.save(division);

        String formato = normalizarFormato(request.getFormato());
        List<CompeticionZona> zonasCreadas = new ArrayList<>();
        if ("LIGA".equals(formato)) {
            crearFase(division, "Liga", "LIGA", 1, null);
        } else if ("ELIMINACION_DIRECTA".equals(formato)) {
            crearFase(division, "Eliminación directa", "ELIMINACION_DIRECTA", 1, request.getCantidadEquipos());
        } else if ("LIGA_PLAYOFF".equals(formato)) {
            crearFase(division, "Liga", "LIGA", 1, null);
            crearFase(division, "Playoffs", "ELIMINACION_DIRECTA", 2, clasificadosPlayoff(request.getCantidadEquipos()));
        } else {
            CompeticionFase faseZonas = crearFase(division, "Fase de zonas", "ZONAS", 1, null);
            int cantidadZonas = request.getCantidadZonas() == null ? 2 : request.getCantidadZonas();
            if (cantidadZonas > request.getCantidadEquipos()) throw new IllegalArgumentException("No puede haber más zonas que equipos");
            for (int i = 0; i < cantidadZonas; i++) {
                CompeticionZona zona = new CompeticionZona();
                zona.setFase(faseZonas); zona.setNombre("Zona " + letraZona(i)); zona.setOrden(i + 1);
                zonasCreadas.add(zonas.save(zona));
            }
            int porZona = request.getClasificadosPorZona() == null ? 2 : request.getClasificadosPorZona();
            if (porZona * cantidadZonas > request.getCantidadEquipos()) throw new IllegalArgumentException("La cantidad de clasificados supera la cantidad de equipos");
            crearFase(division, "Playoffs", "ELIMINACION_DIRECTA", 2, porZona * cantidadZonas);
        }

        for (int i = 1; i <= request.getCantidadEquipos(); i++) {
            CupoEquipo cupo = new CupoEquipo();
            cupo.setDivision(division); cupo.setNumero(i); cupo.setEtiqueta("Equipo " + i);
            if (!zonasCreadas.isEmpty()) cupo.setZona(zonasCreadas.get((i - 1) % zonasCreadas.size()));
            if (Boolean.TRUE.equals(request.getGenerarEquiposPrueba())) {
                cupo.setEquipo(crearEquipoDePrueba(torneo, i, request.getJugadoresPorEquipoPrueba()));
            }
            cupos.save(cupo);
        }
    }

    @Transactional(readOnly = true)
    public EstructuraTorneoDTO obtener(Long idTorneo) {
        if (!torneos.existsById(idTorneo)) throw new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo);
        var resultado = divisiones.findByTorneoIdTorneoOrderByOrden(idTorneo).stream().map(division -> {
            var fasesDto = fases.findByDivisionIdDivisionOrderByOrden(division.getIdDivision()).stream().map(fase ->
                new EstructuraTorneoDTO.Fase(fase.getIdFase(), fase.getNombre(), fase.getTipo(), fase.getOrden(),
                    fase.getClasificados(), fase.getEstado(), zonas.findByFaseIdFaseOrderByOrden(fase.getIdFase()).stream()
                    .map(z -> new EstructuraTorneoDTO.Zona(z.getIdZona(), z.getNombre(), z.getOrden())).toList())).toList();
            var cuposDto = cupos.findByDivisionIdDivisionOrderByNumero(division.getIdDivision()).stream().map(c ->
                new EstructuraTorneoDTO.Cupo(c.getIdCupo(), c.getNumero(), c.getEtiqueta(),
                    c.getZona() == null ? null : c.getZona().getIdZona(), c.getZona() == null ? null : c.getZona().getNombre(),
                    c.getEquipo() == null ? null : c.getEquipo().getIdEquipo(), c.getEquipo() == null ? null : c.getEquipo().getNombre())).toList();
            return new EstructuraTorneoDTO.Division(division.getIdDivision(), division.getNombre(), division.getOrden(), fasesDto, cuposDto);
        }).toList();
        return new EstructuraTorneoDTO(idTorneo, resultado);
    }

    private CompeticionFase crearFase(CompeticionDivision division, String nombre, String tipo, int orden, Integer clasificados) {
        CompeticionFase fase = new CompeticionFase();
        fase.setDivision(division); fase.setNombre(nombre); fase.setTipo(tipo); fase.setOrden(orden); fase.setClasificados(clasificados);
        return fases.save(fase);
    }

    public String normalizarFormato(String valor) {
        String formato = valor == null ? "" : valor.trim().toUpperCase().replace('Ó', 'O').replace('Í', 'I');
        return switch (formato) {
            case "LIGA" -> "LIGA";
            case "ELIMINACION DIRECTA", "ELIMINACION_DIRECTA" -> "ELIMINACION_DIRECTA";
            case "LIGA + PLAYOFFS", "LIGA_PLAYOFF", "LIGA Y PLAYOFFS" -> "LIGA_PLAYOFF";
            case "ZONAS + PLAYOFFS", "ZONAS_PLAYOFF", "GRUPOS Y ELIMINACION" -> "ZONAS_PLAYOFF";
            default -> throw new IllegalArgumentException("El formato seleccionado no es válido");
        };
    }

    private int clasificadosPlayoff(int equipos) {
        if (equipos >= 8) return 8;
        if (equipos >= 4) return 4;
        return 2;
    }

    private String letraZona(int indice) { return String.valueOf((char) ('A' + indice)); }

    private Equipo crearEquipoDePrueba(Torneo torneo, int numero, Integer cantidadJugadores) {
        String[] colores = {"Azul", "Rojo", "Verde", "Amarillo", "Negro", "Blanco", "Naranja", "Violeta"};
        Equipo equipo = new Equipo();
        equipo.setNombre("Equipo " + numero);
        equipo.setDescripcion("Equipo de prueba generado automáticamente. Podés editarlo o reemplazarlo.");
        equipo.setColorCamiseta(colores[(numero - 1) % colores.length]);
        equipo.setBarrio(torneo.getUbicacion());
        equipo.setResponsable(torneo.getPropietario().getNombre());
        equipo.setEstado("ACTIVO");
        equipo.setTorneo(torneo);
        equipo.setCreador(torneo.getPropietario());
        equipo = equipos.save(equipo);

        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo); inscripcion.setTorneo(torneo); inscripcion.setEstado("ACTIVA");
        inscripcion = inscripciones.save(inscripcion);
        int cantidad = cantidadJugadores == null ? 5 : cantidadJugadores;
        String[] posiciones = {"Arquero", "Defensor", "Mediocampista", "Delantero", "Delantero"};
        for (int dorsal = 1; dorsal <= cantidad; dorsal++) {
            Jugador jugador = new Jugador();
            jugador.setNombre("Jugador " + numero + "-" + dorsal);
            jugador.setApellido("Prueba"); jugador.setApodo("J" + numero + dorsal);
            jugador.setFechaNacimiento("2000-01-01"); jugador.setNumeroCamiseta(dorsal);
            jugador.setPosicion(posiciones[(dorsal - 1) % posiciones.length]); jugador.setPiernaHabil(dorsal % 2 == 0 ? "Izquierda" : "Derecha");
            jugador.setEstado("ACTIVO"); jugador.setEquipo(equipo);
            jugador = jugadores.save(jugador);
            InscripcionJugador alta = new InscripcionJugador();
            alta.setInscripcionEquipo(inscripcion); alta.setJugador(jugador); alta.setNumeroCamiseta(dorsal);
            alta.setPosicion(jugador.getPosicion()); alta.setEstado("ACTIVA"); planteles.save(alta);
        }
        return equipo;
    }
}
