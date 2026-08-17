package com.futapp.config;

import com.futapp.model.*;
import com.futapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(
            TorneoRepository torneoRepository,
            EquipoRepository equipoRepository,
            JugadorRepository jugadorRepository,
            FechaRepository fechaRepository,
            PartidoRepository partidoRepository,
            GolRepository golRepository
    ) {
        return args -> {

            if (torneoRepository.count() > 0) {
                return;
            }

            // =========================
            // TORNEO 1 - 12 equipos - 4 fechas completas
            // =========================
            Torneo torneo1 = new Torneo();
            torneo1.setNombre("Torneo Apertura Merlo 2026");
            torneo1.setDescripcion("Torneo activo con 12 equipos y 4 fechas completas");
            torneo1.setUbicacion("Merlo");
            torneo1.setEstado("ACTIVO");
            torneo1.setFormato("Liga");
            torneo1.setCantidadEquipos(12);
            torneo1 = torneoRepository.save(torneo1);

            List<Equipo> equiposT1 = crearEquipos(
                    torneo1,
                    equipoRepository,
                    "Los Pibes FC", "La Banda FC", "Deportivo Padua", "Atlético Libertad",
                    "Villa Club", "Oeste Unido", "Juventud FC", "Santos del Sur",
                    "La Esquina FC", "Fortín Oeste", "Club 25", "Racing de Merlo"
            );

            crearJugadoresPorEquipo(equiposT1, jugadorRepository, 5);
            List<Fecha> fechasT1 = crearFechas(torneo1, fechaRepository, 4);

            crearPartidosConCruces(
                    torneo1, fechasT1.get(0), equiposT1, partidoRepository,
                    new int[][]{
                            {0, 11, 2, 1},
                            {1, 10, 1, 1},
                            {2, 9, 0, 3},
                            {3, 8, 4, 2},
                            {4, 7, 2, 0},
                            {5, 6, 1, 0}
                    }
            );

            crearPartidosConCruces(
                    torneo1, fechasT1.get(1), equiposT1, partidoRepository,
                    new int[][]{
                            {0, 10, 1, 0},
                            {11, 9, 2, 2},
                            {1, 8, 3, 1},
                            {2, 7, 0, 0},
                            {3, 6, 1, 2},
                            {4, 5, 2, 1}
                    }
            );

            crearPartidosConCruces(
                    torneo1, fechasT1.get(2), equiposT1, partidoRepository,
                    new int[][]{
                            {0, 9, 0, 1},
                            {10, 8, 2, 0},
                            {11, 7, 1, 3},
                            {1, 6, 2, 2},
                            {2, 5, 4, 1},
                            {3, 4, 1, 1}
                    }
            );

            crearPartidosConCruces(
                    torneo1, fechasT1.get(3), equiposT1, partidoRepository,
                    new int[][]{
                            {0, 8, 3, 0},
                            {9, 7, 1, 2},
                            {10, 6, 2, 1},
                            {11, 5, 0, 1},
                            {1, 4, 2, 2},
                            {2, 3, 5, 1}
                    }
            );

            // =========================
            // TORNEO 2 - 8 equipos - FINALIZADO - 7 fechas completas
            // =========================
            Torneo torneo2 = new Torneo();
            torneo2.setNombre("Copa Zona Oeste 2026");
            torneo2.setDescripcion("Torneo finalizado con 8 equipos");
            torneo2.setUbicacion("Zona Oeste");
            torneo2.setEstado("FINALIZADO");
            torneo2.setFormato("Liga");
            torneo2.setCantidadEquipos(8);
            torneo2 = torneoRepository.save(torneo2);

            List<Equipo> equiposT2 = crearEquipos(
                    torneo2,
                    equipoRepository,
                    "Lomas FC", "Barrio Unido", "Deportivo Centro", "Estrella Azul",
                    "Nueva Era", "Los del Puente", "Sur FC", "Alianza Oeste"
            );

            crearJugadoresPorEquipo(equiposT2, jugadorRepository, 5);
            List<Fecha> fechasT2 = crearFechas(torneo2, fechaRepository, 7);

            crearPartidosConCruces(
                    torneo2, fechasT2.get(0), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 7, 1, 0},
                            {1, 6, 2, 1},
                            {2, 5, 0, 0},
                            {3, 4, 3, 1}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(1), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 6, 2, 2},
                            {7, 5, 1, 0},
                            {1, 4, 0, 1},
                            {2, 3, 4, 2}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(2), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 5, 3, 0},
                            {6, 4, 1, 1},
                            {7, 3, 2, 0},
                            {1, 2, 0, 2}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(3), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 4, 1, 1},
                            {5, 3, 2, 0},
                            {6, 2, 0, 1},
                            {7, 1, 3, 2}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(4), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 3, 2, 1},
                            {4, 2, 0, 0},
                            {5, 1, 1, 2},
                            {6, 7, 2, 3}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(5), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 2, 0, 1},
                            {3, 1, 3, 3},
                            {4, 7, 2, 0},
                            {5, 6, 1, 0}
                    }
            );

            crearPartidosConCruces(
                    torneo2, fechasT2.get(6), equiposT2, partidoRepository,
                    new int[][]{
                            {0, 1, 2, 0},
                            {2, 7, 1, 1},
                            {3, 6, 0, 2},
                            {4, 5, 4, 1}
                    }
            );

            // =========================
            // TORNEO 3 - 10 equipos - 2 fechas
            // =========================
            Torneo torneo3 = new Torneo();
            torneo3.setNombre("Liga Amateur Buenos Aires 2026");
            torneo3.setDescripcion("Torneo en progreso con 10 equipos y 2 fechas");
            torneo3.setUbicacion("Buenos Aires");
            torneo3.setEstado("ACTIVO");
            torneo3.setFormato("Liga");
            torneo3.setCantidadEquipos(10);
            torneo3 = torneoRepository.save(torneo3);

            List<Equipo> equiposT3 = crearEquipos(
                    torneo3,
                    equipoRepository,
                    "Cañada FC", "Los Guerreros", "Villa Norte", "Atlético Parque", "Defensores del Oeste",
                    "Unión del Sur", "Los Amigos", "Sporting Merlo", "Juventud Unida", "Barracas Amateur"
            );

            crearJugadoresPorEquipo(equiposT3, jugadorRepository, 5);
            List<Fecha> fechasT3 = crearFechas(torneo3, fechaRepository, 2);

            crearPartidosConCruces(
                    torneo3, fechasT3.get(0), equiposT3, partidoRepository,
                    new int[][]{
                            {0, 9, 1, 0},
                            {1, 8, 0, 2},
                            {2, 7, 2, 2},
                            {3, 6, 3, 1},
                            {4, 5, 1, 1}
                    }
            );

            crearPartidosConCruces(
                    torneo3, fechasT3.get(1), equiposT3, partidoRepository,
                    new int[][]{
                            {0, 8, 0, 0},
                            {9, 7, 2, 1},
                            {1, 6, 1, 3},
                            {2, 5, 4, 0},
                            {3, 4, 2, 2}
                    }
            );

            // Goles demo en base a resultados
            generarGolesDemo(partidoRepository, jugadorRepository, golRepository);
        };
    }

    private List<Equipo> crearEquipos(Torneo torneo, EquipoRepository equipoRepository, String... nombres) {
        List<Equipo> equipos = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            Equipo equipo = new Equipo();
            equipo.setNombre(nombres[i]);
            equipo.setEscudo("escudo_" + (i + 1) + ".png");
            equipo.setColorCamiseta("Color " + (i + 1));
            equipo.setBarrio("Barrio " + (i + 1));
            equipo.setResponsable("Responsable " + (i + 1));
            equipo.setTelefonoContacto("11000000" + (i + 1));
            equipo.setEstado("ACTIVO");
            equipo.setTorneo(torneo);
            equipos.add(equipoRepository.save(equipo));
        }
        return equipos;
    }

    private void crearJugadoresPorEquipo(List<Equipo> equipos, JugadorRepository jugadorRepository, int cantidad) {
        for (Equipo equipo : equipos) {
            for (int i = 1; i <= cantidad; i++) {
                Jugador jugador = new Jugador();
                jugador.setNombre("Jugador" + i);
                jugador.setApellido(equipo.getNombre().replaceAll("\\s+", ""));
                jugador.setDni(equipo.getIdEquipo() + "00" + i);
                jugador.setFechaNacimiento("1998-01-0" + Math.min(i, 9));
                jugador.setNumeroCamiseta(i);
                jugador.setPosicion(i == 1 ? "Arquero" : (i <= 3 ? "Defensor" : "Delantero"));
                jugador.setFoto("jugador_" + equipo.getIdEquipo() + "_" + i + ".png");
                jugador.setEstado("ACTIVO");
                jugador.setEquipo(equipo);
                jugadorRepository.save(jugador);
            }
        }
    }

    private List<Fecha> crearFechas(Torneo torneo, FechaRepository fechaRepository, int cantidad) {
        List<Fecha> fechas = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            Fecha fecha = new Fecha();
            fecha.setNumeroFecha(i);
            fecha.setDescripcion("Fecha " + i);
            fecha.setEstado("ACTIVA");
            fecha.setTorneo(torneo);
            fechas.add(fechaRepository.save(fecha));
        }
        return fechas;
    }

    private void crearPartidosConCruces(
            Torneo torneo,
            Fecha fecha,
            List<Equipo> equipos,
            PartidoRepository partidoRepository,
            int[][] cruces
    ) {
        for (int[] cruce : cruces) {
            int idxLocal = cruce[0];
            int idxVisitante = cruce[1];
            int golesLocal = cruce[2];
            int golesVisitante = cruce[3];

            Partido partido = new Partido();
            partido.setFechaHora("2026-04-" + (10 + fecha.getNumeroFecha()) + " 18:00");
            partido.setEstado("FINALIZADO");
            partido.setGolesLocal(golesLocal);
            partido.setGolesVisitante(golesVisitante);
            partido.setObservaciones("Partido de la fecha " + fecha.getNumeroFecha());
            partido.setTorneo(torneo);
            partido.setFecha(fecha);
            partido.setEquipoLocal(equipos.get(idxLocal));
            partido.setEquipoVisitante(equipos.get(idxVisitante));

            partidoRepository.save(partido);
        }
    }

    private void generarGolesDemo(
            PartidoRepository partidoRepository,
            JugadorRepository jugadorRepository,
            GolRepository golRepository
    ) {
        List<Partido> partidos = partidoRepository.findAll();

        for (Partido partido : partidos) {
            int golesLocal = partido.getGolesLocal() != null ? partido.getGolesLocal() : 0;
            int golesVisitante = partido.getGolesVisitante() != null ? partido.getGolesVisitante() : 0;

            List<Jugador> jugadoresLocal = jugadorRepository.findByEquipoIdEquipo(partido.getEquipoLocal().getIdEquipo());
            List<Jugador> jugadoresVisitante = jugadorRepository.findByEquipoIdEquipo(partido.getEquipoVisitante().getIdEquipo());

            for (int i = 0; i < golesLocal; i++) {
                Gol gol = new Gol();
                gol.setMinuto(10 + i * 7);
                gol.setTipoGol("Jugada");
                gol.setObservacion("Gol local");
                gol.setPartido(partido);
                gol.setJugador(jugadoresLocal.get(Math.min(i, jugadoresLocal.size() - 1)));
                gol.setEquipo(partido.getEquipoLocal());
                golRepository.save(gol);
            }

            for (int i = 0; i < golesVisitante; i++) {
                Gol gol = new Gol();
                gol.setMinuto(15 + i * 8);
                gol.setTipoGol("Jugada");
                gol.setObservacion("Gol visitante");
                gol.setPartido(partido);
                gol.setJugador(jugadoresVisitante.get(Math.min(i, jugadoresVisitante.size() - 1)));
                gol.setEquipo(partido.getEquipoVisitante());
                golRepository.save(gol);
            }
        }
    }
}