package com.futapp.service;

import com.futapp.dto.PartidoRequestDTO;
import com.futapp.dto.PartidoResponseDTO;
import com.futapp.dto.ResultadoPartidoDTO;
import com.futapp.dto.FixtureRequestDTO;
import com.futapp.dto.MvpPartidoRequestDTO;
import com.futapp.dto.EstadoPartidoRequestDTO;
import com.futapp.dto.JugadorPartidoDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Fecha;
import com.futapp.model.Partido;
import com.futapp.model.Torneo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.FechaRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.GolRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.TarjetaRepository;
import com.futapp.repository.CompeticionDivisionRepository;
import com.futapp.repository.CupoEquipoRepository;
import com.futapp.repository.CompeticionFaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PartidoService {

    private static final java.util.Set<String> ESTADOS = java.util.Set.of(
            "PROGRAMADO", "EN_JUEGO", "FINALIZADO", "SUSPENDIDO", "CANCELADO");

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final FechaRepository fechaRepository;
    private final EquipoRepository equipoRepository;
    private final GolRepository golRepository;
    private final TarjetaRepository tarjetaRepository;
    private final TorneoAuthorizationService authorization;
    private final NotificacionService notificaciones;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final JugadorRepository jugadores;
    private final CompeticionDivisionRepository divisiones;
    private final CupoEquipoRepository cupos;
    private final CompeticionFaseRepository fases;
    private final TablaPosicionService tablaPosiciones;

    public PartidoService(PartidoRepository partidoRepository,
                          TorneoRepository torneoRepository,
                          FechaRepository fechaRepository,
                          EquipoRepository equipoRepository,
                          GolRepository golRepository,
                          TarjetaRepository tarjetaRepository,
                          TorneoAuthorizationService authorization,
                          NotificacionService notificaciones,
                          InscripcionEquipoRepository inscripcionesEquipo,
                          InscripcionJugadorRepository inscripcionesJugador,
                          JugadorRepository jugadores,
                          CompeticionDivisionRepository divisiones,
                          CupoEquipoRepository cupos,
                          CompeticionFaseRepository fases,
                          TablaPosicionService tablaPosiciones) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.fechaRepository = fechaRepository;
        this.equipoRepository = equipoRepository;
        this.golRepository = golRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.authorization = authorization;
        this.notificaciones = notificaciones;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.jugadores = jugadores;
        this.divisiones = divisiones;
        this.cupos = cupos;
        this.fases = fases;
        this.tablaPosiciones = tablaPosiciones;
    }

    public List<PartidoResponseDTO> listarPartidos() {
        return partidoRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO obtenerPartidoPorId(Long id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));

        return convertirAResponseDTO(partido);
    }

    public List<PartidoResponseDTO> listarPartidosPorTorneo(Long idTorneo) {
        return partidoRepository.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PartidoResponseDTO> listarPartidosPorFecha(Long idFecha) {
        return partidoRepository.findByFechaIdFecha(idFecha)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO crearPartido(PartidoRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        if (!"PROGRAMADO".equals(normalizarEstado(requestDTO.getEstado()))) {
            throw new IllegalArgumentException("Los partidos nuevos deben crearse como programados");
        }
        Partido partido = new Partido();
        cargarDatosPartido(partido, requestDTO);
        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, "FINALIZADO".equalsIgnoreCase(partido.getEstado()));
        return convertirAResponseDTO(partido);
    }

    public List<JugadorPartidoDTO> listarJugadoresDelPartido(Long idPartido) {
        Partido partido = partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + idPartido));
        return java.util.stream.Stream.of(partido.getEquipoLocal(), partido.getEquipoVisitante())
                .flatMap(equipo -> inscripcionesJugador
                        .findByInscripcionEquipoEquipoIdEquipoAndInscripcionEquipoTorneoIdTorneoAndEstado(
                                equipo.getIdEquipo(), partido.getTorneo().getIdTorneo(), "ACTIVA").stream())
                .map(item -> new JugadorPartidoDTO(item.getJugador().getIdJugador(), item.getJugador().getNombre(),
                        item.getJugador().getApellido(), item.getJugador().getApodo(), item.getJugador().getFoto(),
                        item.getInscripcionEquipo().getEquipo().getIdEquipo(),
                        item.getInscripcionEquipo().getEquipo().getNombre(), item.getNumeroCamiseta()))
                .sorted(java.util.Comparator.comparing(JugadorPartidoDTO::nombreEquipo)
                        .thenComparing(item -> item.numeroCamiseta() == null ? 999 : item.numeroCamiseta()))
                .toList();
    }

    @Transactional
    public List<PartidoResponseDTO> generarFixture(Long idTorneo, FixtureRequestDTO request) {
        authorization.verificarGestion(idTorneo);
        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo));

        if (!partidoRepository.findByTorneoIdTorneo(idTorneo).isEmpty()) {
            throw new IllegalArgumentException("El fixture automático solo se puede generar si el torneo todavía no tiene partidos");
        }

        List<Equipo> equipos = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(idTorneo, "ACTIVA")
                .stream().map(inscripcion -> inscripcion.getEquipo()).collect(Collectors.toCollection(ArrayList::new));
        equipos.sort(Comparator.comparing(Equipo::getIdEquipo));
        if (equipos.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 equipos para generar el fixture");
        }

        if ("ELIMINACIÓN DIRECTA".equalsIgnoreCase(torneo.getFormato())) {
            equipos = ordenarEquiposParaLlave(equipos, request.ordenEquipos());
            return generarPrimeraLlave(torneo, equipos, request);
        }
        if ("ZONAS + PLAYOFFS".equalsIgnoreCase(torneo.getFormato())) {
            return generarFixturePorZonas(torneo, equipos, request);
        }

        if (equipos.size() % 2 != 0) {
            equipos.add(null);
        }

        List<Equipo> rotacion = new ArrayList<>(equipos);
        List<Partido> partidosGenerados = new ArrayList<>();
        int cantidadFechas = rotacion.size() - 1;
        List<Fecha> fechasExistentes = new ArrayList<>(fechaRepository.findByTorneoIdTorneo(idTorneo));
        fechasExistentes.sort(Comparator.comparing(Fecha::getNumeroFecha));
        int siguienteNumero = fechasExistentes.stream()
                .map(Fecha::getNumeroFecha)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        int totalFechas = cantidadFechas * (Boolean.TRUE.equals(torneo.getIdaVuelta()) ? 2 : 1);
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int ronda = 0; ronda < totalFechas; ronda++) {
            int rondaBase = ronda % cantidadFechas;
            boolean vuelta = ronda >= cantidadFechas;
            Fecha fecha;
            if (ronda < fechasExistentes.size()) {
                fecha = fechasExistentes.get(ronda);
            } else {
                fecha = new Fecha();
                fecha.setNumeroFecha(siguienteNumero++);
                fecha.setDescripcion("Fecha " + fecha.getNumeroFecha());
                fecha.setEstado(ronda == 0 ? "ACTIVA" : "PENDIENTE");
                fecha.setTorneo(torneo);
                fecha = fechaRepository.save(fecha);
            }

            int indiceHorario = 0;
            for (int indice = 0; indice < rotacion.size() / 2; indice++) {
                Equipo primero = rotacion.get(indice);
                Equipo segundo = rotacion.get(rotacion.size() - 1 - indice);
                if (primero == null || segundo == null) {
                    continue;
                }

                boolean invertirLocalia = (rondaBase + indice) % 2 != 0;
                if (vuelta) invertirLocalia = !invertirLocalia;
                Partido partido = new Partido();
                partido.setTorneo(torneo);
                partido.setFecha(fecha);
                partido.setEquipoLocal(invertirLocalia ? segundo : primero);
                partido.setEquipoVisitante(invertirLocalia ? primero : segundo);
                LocalDateTime fechaHora = LocalDateTime.of(
                        request.fechaInicio().plusDays((long) ronda * request.diasEntreJornadas()),
                        request.horaInicio()).plusMinutes((long) indiceHorario * request.minutosEntrePartidos());
                partido.setFechaHora(fechaHora.format(formatoFechaHora));
                partido.setEstado("PROGRAMADO");
                partido.setGolesLocal(0);
                partido.setGolesVisitante(0);
                partido.setObservaciones("Horario a confirmar");
                partido.setCancha(normalizarOpcional(request.cancha()));
                partidosGenerados.add(partidoRepository.save(partido));
                indiceHorario++;
            }

            Equipo ultimo = rotacion.remove(rotacion.size() - 1);
            rotacion.add(1, ultimo);
        }

        return partidosGenerados.stream().map(this::convertirAResponseDTO).toList();
    }

    private List<PartidoResponseDTO> generarPrimeraLlave(Torneo torneo, List<Equipo> equipos, FixtureRequestDTO request) {
        List<Partido> generados = new ArrayList<>();
        boolean potenciaDeDos = (equipos.size() & (equipos.size() - 1)) == 0;
        String ronda = potenciaDeDos ? nombreRonda(equipos.size()) : "Primera ronda eliminatoria";
        Fecha fecha = crearFecha(torneo, 1, ronda, true);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int partidos = potenciaDeDos ? equipos.size() / 2 : equipos.size() - Integer.highestOneBit(equipos.size());
        for (int i = 0; i < partidos; i++) {
            generados.add(crearPartidoAutomatico(torneo, fecha, equipos.get(i), equipos.get(equipos.size() - 1 - i),
                    LocalDateTime.of(request.fechaInicio(), request.horaInicio())
                            .plusMinutes((long) i * request.minutosEntrePartidos()).format(formato),
                    request.cancha(), ronda));
        }
        return generados.stream().map(this::convertirAResponseDTO).toList();
    }

    private List<Equipo> ordenarEquiposParaLlave(List<Equipo> equipos, List<Long> ordenSolicitado) {
        if (ordenSolicitado == null || ordenSolicitado.isEmpty()) return equipos;
        if (ordenSolicitado.size() != equipos.size() || ordenSolicitado.stream().distinct().count() != equipos.size()) {
            throw new IllegalArgumentException("El orden de la llave debe incluir una sola vez a todos los equipos");
        }
        var porId = equipos.stream().collect(Collectors.toMap(Equipo::getIdEquipo, equipo -> equipo));
        if (!porId.keySet().containsAll(ordenSolicitado)) {
            throw new IllegalArgumentException("La llave contiene un equipo que no pertenece al torneo");
        }
        return ordenSolicitado.stream().map(porId::get).collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public List<PartidoResponseDTO> generarSiguienteLlave(Long idTorneo) {
        authorization.verificarGestion(idTorneo);
        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo));
        List<Partido> todos = partidoRepository.findByTorneoIdTorneo(idTorneo);
        if (todos.isEmpty()) throw new IllegalArgumentException("Primero generá la llave inicial");
        List<Partido> eliminatorios = todos.stream().filter(this::esPartidoEliminatorio).toList();
        if (eliminatorios.isEmpty()) {
            if (!torneo.getFormato().toUpperCase().contains("PLAYOFF")) {
                throw new IllegalArgumentException("Esta acción corresponde a torneos con eliminación directa o playoffs");
            }
            return generarPlayoffsDesdeClasificacion(torneo, todos);
        }
        todos = eliminatorios;
        int ultimaRonda = todos.stream().map(p -> p.getFecha().getNumeroFecha()).max(Integer::compareTo).orElseThrow();
        List<Partido> ronda = todos.stream().filter(p -> p.getFecha().getNumeroFecha() == ultimaRonda)
                .sorted(Comparator.comparing(Partido::getIdPartido)).toList();
        if (ronda.stream().anyMatch(p -> !"FINALIZADO".equalsIgnoreCase(p.getEstado()))) {
            throw new IllegalArgumentException("Cargá todos los resultados de la ronda antes de generar la siguiente");
        }
        List<Equipo> ganadores = ronda.stream().map(this::ganadorEliminatoria).collect(Collectors.toCollection(ArrayList::new));
        if (ronda.stream().allMatch(p -> "Primera ronda eliminatoria".equals(p.getObservaciones()))) {
            java.util.Set<Long> participantes = ronda.stream()
                    .flatMap(p -> java.util.stream.Stream.of(p.getEquipoLocal().getIdEquipo(), p.getEquipoVisitante().getIdEquipo()))
                    .collect(Collectors.toSet());
            List<Equipo> participantesLlave;
            if ("ELIMINACIÓN DIRECTA".equalsIgnoreCase(torneo.getFormato())) {
                participantesLlave = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(idTorneo, "ACTIVA").stream().map(i -> i.getEquipo()).toList();
            } else {
                var division = divisiones.findByTorneoIdTorneoOrderByOrden(idTorneo).stream().findFirst().orElseThrow();
                int clasificados = fases.findByDivisionIdDivisionOrderByOrden(division.getIdDivision()).stream()
                        .filter(f -> "ELIMINACION_DIRECTA".equals(f.getTipo())).map(f -> f.getClasificados() == null ? 4 : f.getClasificados()).findFirst().orElse(4);
                participantesLlave = obtenerClasificados(torneo, clasificados, division.getIdDivision());
            }
            participantesLlave.stream()
                    .filter(e -> !participantes.contains(e.getIdEquipo())).sorted(Comparator.comparing(Equipo::getIdEquipo))
                    .forEach(ganadores::add);
        }
        if (ganadores.size() == 1) throw new IllegalArgumentException("La llave ya tiene un campeón");

        String nombre = nombreRonda(ganadores.size());
        Fecha fecha = crearFecha(torneo, ultimaRonda + 1, nombre, true);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime inicio = ronda.stream().map(Partido::getFechaHora).filter(java.util.Objects::nonNull)
                .map(valor -> LocalDateTime.parse(valor, formato)).max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()).plusDays(7);
        List<Partido> generados = new ArrayList<>();
        for (int i = 0; i < ganadores.size() / 2; i++) {
            generados.add(crearPartidoAutomatico(torneo, fecha, ganadores.get(i), ganadores.get(ganadores.size() - 1 - i),
                    inicio.plusMinutes(i * 90L).format(formato), ronda.get(0).getCancha(), nombre));
        }
        return generados.stream().map(this::convertirAResponseDTO).toList();
    }

    private List<PartidoResponseDTO> generarPlayoffsDesdeClasificacion(Torneo torneo, List<Partido> faseRegular) {
        if (faseRegular.stream().anyMatch(p -> !"FINALIZADO".equalsIgnoreCase(p.getEstado()))) {
            throw new IllegalArgumentException("Finalizá todos los partidos de la fase clasificatoria antes de generar los playoffs");
        }
        var division = divisiones.findByTorneoIdTorneoOrderByOrden(torneo.getIdTorneo()).stream().findFirst().orElseThrow();
        int clasificados = fases.findByDivisionIdDivisionOrderByOrden(division.getIdDivision()).stream()
                .filter(f -> "ELIMINACION_DIRECTA".equals(f.getTipo())).map(f -> f.getClasificados() == null ? 4 : f.getClasificados())
                .findFirst().orElse(4);
        List<Equipo> equipos = obtenerClasificados(torneo, clasificados, division.getIdDivision());
        if (equipos.size() < 2) throw new IllegalArgumentException("No hay suficientes equipos clasificados");
        return crearRondaEliminatoria(torneo, equipos, faseRegular);
    }

    private List<Equipo> obtenerClasificados(Torneo torneo, int clasificados, Long idDivision) {
        List<Long> idsClasificados = new ArrayList<>();
        var tabla = tablaPosiciones.obtenerTablaPorTorneo(torneo.getIdTorneo());
        if ("ZONAS + PLAYOFFS".equalsIgnoreCase(torneo.getFormato())) {
            var zonasTorneo = cupos.findByDivisionIdDivisionOrderByNumero(idDivision).stream()
                    .filter(c -> c.getZona() != null && c.getEquipo() != null)
                    .collect(Collectors.groupingBy(c -> c.getZona().getNombre(), java.util.LinkedHashMap::new,
                            Collectors.mapping(c -> c.getEquipo().getIdEquipo(), Collectors.toList())));
            int porZona = Math.max(1, clasificados / zonasTorneo.size());
            for (var idsZona : zonasTorneo.values()) {
                tabla.stream().filter(f -> idsZona.contains(f.getIdEquipo())).limit(porZona)
                        .forEach(f -> idsClasificados.add(f.getIdEquipo()));
            }
        } else {
            tabla.stream().limit(clasificados).forEach(f -> idsClasificados.add(f.getIdEquipo()));
        }
        var porId = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(torneo.getIdTorneo(), "ACTIVA").stream()
                .map(i -> i.getEquipo()).collect(Collectors.toMap(Equipo::getIdEquipo, e -> e));
        List<Equipo> equipos = idsClasificados.stream().map(porId::get).filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        return equipos;
    }

    private List<PartidoResponseDTO> crearRondaEliminatoria(Torneo torneo, List<Equipo> equipos, List<Partido> anteriores) {
        int numero = anteriores.stream().map(p -> p.getFecha().getNumeroFecha()).max(Integer::compareTo).orElse(0) + 1;
        boolean potenciaDeDos = (equipos.size() & (equipos.size() - 1)) == 0;
        String nombre = potenciaDeDos ? nombreRonda(equipos.size()) : "Primera ronda eliminatoria";
        Fecha fecha = crearFecha(torneo, numero, nombre, true);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime inicio = anteriores.stream().map(Partido::getFechaHora).filter(java.util.Objects::nonNull)
                .map(v -> LocalDateTime.parse(v, formato)).max(LocalDateTime::compareTo).orElse(LocalDateTime.now()).plusDays(7);
        String cancha = anteriores.stream().map(Partido::getCancha).filter(java.util.Objects::nonNull).findFirst().orElse(null);
        List<Partido> creados = new ArrayList<>();
        int partidos = potenciaDeDos ? equipos.size() / 2 : equipos.size() - Integer.highestOneBit(equipos.size());
        for (int i = 0; i < partidos; i++) {
            creados.add(crearPartidoAutomatico(torneo, fecha, equipos.get(i), equipos.get(equipos.size() - 1 - i),
                    inicio.plusMinutes(i * 90L).format(formato), cancha, nombre));
        }
        return creados.stream().map(this::convertirAResponseDTO).toList();
    }

    private boolean esPartidoEliminatorio(Partido partido) {
        String detalle = partido.getObservaciones();
        return detalle != null && (detalle.equals("Final") || detalle.equals("Semifinales")
                || detalle.equals("Cuartos de final") || detalle.equals("Octavos de final")
                || detalle.equals("Primera ronda eliminatoria"));
    }

    private Equipo ganadorEliminatoria(Partido partido) {
        if (partido.getGolesLocal() > partido.getGolesVisitante()) return partido.getEquipoLocal();
        if (partido.getGolesVisitante() > partido.getGolesLocal()) return partido.getEquipoVisitante();
        if (partido.getPenalesLocal() == null || partido.getPenalesVisitante() == null
                || partido.getPenalesLocal().equals(partido.getPenalesVisitante())) {
            throw new IllegalArgumentException("Definí los penales de cada partido empatado antes de avanzar");
        }
        return partido.getPenalesLocal() > partido.getPenalesVisitante() ? partido.getEquipoLocal() : partido.getEquipoVisitante();
    }

    private List<PartidoResponseDTO> generarFixturePorZonas(Torneo torneo, List<Equipo> equiposActivos, FixtureRequestDTO request) {
        var division = divisiones.findByTorneoIdTorneoOrderByOrden(torneo.getIdTorneo()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El torneo no tiene una estructura de zonas"));
        var porZona = cupos.findByDivisionIdDivisionOrderByNumero(division.getIdDivision()).stream()
                .filter(c -> c.getZona() != null && c.getEquipo() != null
                        && equiposActivos.stream().anyMatch(e -> e.getIdEquipo().equals(c.getEquipo().getIdEquipo())))
                .collect(Collectors.groupingBy(c -> c.getZona().getNombre(), java.util.LinkedHashMap::new,
                        Collectors.mapping(c -> c.getEquipo(), Collectors.toCollection(ArrayList::new))));
        if (porZona.size() < 2) throw new IllegalArgumentException("No se encontraron los equipos distribuidos en sus zonas");

        List<List<List<Equipo[]>>> calendarios = new ArrayList<>();
        int maxRondas = 0;
        for (List<Equipo> zona : porZona.values()) {
            var calendario = rondasLiga(zona);
            calendarios.add(calendario);
            maxRondas = Math.max(maxRondas, calendario.size());
        }
        List<Partido> generados = new ArrayList<>();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int ronda = 0; ronda < maxRondas; ronda++) {
            Fecha fecha = crearFecha(torneo, ronda + 1, "Fecha " + (ronda + 1) + " · Zonas", ronda == 0);
            int horario = 0;
            int zonaIndex = 0;
            for (var calendario : calendarios) {
                if (ronda < calendario.size()) {
                    String zonaNombre = new ArrayList<>(porZona.keySet()).get(zonaIndex);
                    for (Equipo[] cruce : calendario.get(ronda)) {
                        generados.add(crearPartidoAutomatico(torneo, fecha, cruce[0], cruce[1],
                                LocalDateTime.of(request.fechaInicio().plusDays((long) ronda * request.diasEntreJornadas()), request.horaInicio())
                                        .plusMinutes((long) horario++ * request.minutosEntrePartidos()).format(formato),
                                request.cancha(), zonaNombre));
                    }
                }
                zonaIndex++;
            }
        }
        return generados.stream().map(this::convertirAResponseDTO).toList();
    }

    private List<List<Equipo[]>> rondasLiga(List<Equipo> originales) {
        List<Equipo> rotacion = new ArrayList<>(originales);
        if (rotacion.size() % 2 != 0) rotacion.add(null);
        List<List<Equipo[]>> rondas = new ArrayList<>();
        for (int ronda = 0; ronda < rotacion.size() - 1; ronda++) {
            List<Equipo[]> cruces = new ArrayList<>();
            for (int i = 0; i < rotacion.size() / 2; i++) {
                Equipo a = rotacion.get(i), b = rotacion.get(rotacion.size() - 1 - i);
                if (a != null && b != null) cruces.add((ronda + i) % 2 == 0 ? new Equipo[]{a, b} : new Equipo[]{b, a});
            }
            rondas.add(cruces);
            Equipo ultimo = rotacion.remove(rotacion.size() - 1);
            rotacion.add(1, ultimo);
        }
        return rondas;
    }

    private Fecha crearFecha(Torneo torneo, int numero, String descripcion, boolean activa) {
        Fecha fecha = fechaRepository.findByTorneoIdTorneoAndNumeroFecha(torneo.getIdTorneo(), numero).orElseGet(Fecha::new);
        fecha.setNumeroFecha(numero); fecha.setDescripcion(descripcion);
        fecha.setEstado(activa ? "ACTIVA" : "PENDIENTE"); fecha.setTorneo(torneo);
        return fechaRepository.save(fecha);
    }

    private Partido crearPartidoAutomatico(Torneo torneo, Fecha fecha, Equipo local, Equipo visitante,
                                            String fechaHora, String cancha, String detalle) {
        Partido partido = new Partido();
        partido.setTorneo(torneo); partido.setFecha(fecha); partido.setEquipoLocal(local); partido.setEquipoVisitante(visitante);
        partido.setFechaHora(fechaHora); partido.setEstado("PROGRAMADO"); partido.setGolesLocal(0); partido.setGolesVisitante(0);
        partido.setObservaciones(detalle); partido.setCancha(normalizarOpcional(cancha));
        return partidoRepository.save(partido);
    }

    private String nombreRonda(int equipos) {
        if (equipos <= 2) return "Final";
        if (equipos <= 4) return "Semifinales";
        if (equipos <= 8) return "Cuartos de final";
        if (equipos <= 16) return "Octavos de final";
        return "Primera ronda eliminatoria";
    }

    public PartidoResponseDTO actualizarPartido(Long id, PartidoRequestDTO requestDTO) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        authorization.verificarGestion(partido.getTorneo());

        String estadoAnterior = partido.getEstado();
        validarTransicion(estadoAnterior, requestDTO.getEstado(), false);
        if ("FINALIZADO".equalsIgnoreCase(estadoAnterior)
                && (!java.util.Objects.equals(partido.getGolesLocal(), requestDTO.getGolesLocal())
                || !java.util.Objects.equals(partido.getGolesVisitante(), requestDTO.getGolesVisitante()))) {
            throw new IllegalArgumentException("Editá el marcador desde la opción Cargar resultado");
        }
        if ("FINALIZADO".equalsIgnoreCase(estadoAnterior)
                && (!partido.getEquipoLocal().getIdEquipo().equals(requestDTO.getIdEquipoLocal())
                || !partido.getEquipoVisitante().getIdEquipo().equals(requestDTO.getIdEquipoVisitante()))) {
            throw new IllegalArgumentException("No se pueden cambiar los equipos de un partido finalizado");
        }

        cargarDatosPartido(partido, requestDTO);
        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, "FINALIZADO".equalsIgnoreCase(partido.getEstado()));
        return convertirAResponseDTO(partido);
    }

    public void eliminarPartido(Long id) {
        if (!partidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + id);
        }
        authorization.verificarGestion(partidoRepository.findById(id).orElseThrow().getTorneo());
        if (golRepository.existsByPartidoIdPartido(id) || tarjetaRepository.existsByPartidoIdPartido(id)) {
            throw new IllegalArgumentException("No se puede eliminar el partido porque tiene goles o tarjetas cargados");
        }
        partidoRepository.deleteById(id);
    }

    @Transactional
    public void eliminarFixture(Long idTorneo) {
        authorization.verificarGestion(idTorneo);
        List<Partido> partidos = partidoRepository.findByTorneoIdTorneo(idTorneo);
        if (partidos.isEmpty()) throw new IllegalArgumentException("El torneo todavía no tiene un fixture");
        if (partidos.stream().anyMatch(partido -> "FINALIZADO".equalsIgnoreCase(partido.getEstado()))) {
            throw new IllegalArgumentException("No se puede rehacer el fixture porque ya hay resultados cargados");
        }
        if (golRepository.existsByPartidoTorneoIdTorneo(idTorneo)
                || tarjetaRepository.existsByPartidoTorneoIdTorneo(idTorneo)) {
            throw new IllegalArgumentException("No se puede rehacer el fixture porque ya tiene goles o tarjetas");
        }
        partidoRepository.deleteAll(partidos);
    }

    private void cargarDatosPartido(Partido partido, PartidoRequestDTO requestDTO) {
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        Fecha fecha = fechaRepository.findById(requestDTO.getIdFecha())
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada con id: " + requestDTO.getIdFecha()));

        Equipo equipoLocal = equipoRepository.findById(requestDTO.getIdEquipoLocal())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo local no encontrado con id: " + requestDTO.getIdEquipoLocal()));

        Equipo equipoVisitante = equipoRepository.findById(requestDTO.getIdEquipoVisitante())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo visitante no encontrado con id: " + requestDTO.getIdEquipoVisitante()));

        validarReglasDeNegocio(requestDTO, torneo, fecha, equipoLocal, equipoVisitante, partido.getIdPartido());

        partido.setFechaHora(requestDTO.getFechaHora());
        partido.setEstado(normalizarEstado(requestDTO.getEstado()));
        partido.setGolesLocal(requestDTO.getGolesLocal());
        partido.setGolesVisitante(requestDTO.getGolesVisitante());
        partido.setObservaciones(requestDTO.getObservaciones());
        partido.setCancha(normalizarOpcional(requestDTO.getCancha()));
        partido.setTorneo(torneo);
        partido.setFecha(fecha);
        partido.setEquipoLocal(equipoLocal);
        partido.setEquipoVisitante(equipoVisitante);
    }

    private void validarReglasDeNegocio(PartidoRequestDTO requestDTO,
                                        Torneo torneo,
                                        Fecha fecha,
                                        Equipo equipoLocal,
                                        Equipo equipoVisitante,
                                        Long partidoActualId) {

        if (equipoLocal.getIdEquipo().equals(equipoVisitante.getIdEquipo())) {
            throw new IllegalArgumentException("El equipo local y el visitante no pueden ser el mismo");
        }

        if (!inscripcionesEquipo.existsByEquipoIdEquipoAndTorneoIdTorneo(equipoLocal.getIdEquipo(), torneo.getIdTorneo())) {
            throw new IllegalArgumentException("El equipo local no pertenece al torneo indicado");
        }

        if (!inscripcionesEquipo.existsByEquipoIdEquipoAndTorneoIdTorneo(equipoVisitante.getIdEquipo(), torneo.getIdTorneo())) {
            throw new IllegalArgumentException("El equipo visitante no pertenece al torneo indicado");
        }

        if (!fecha.getTorneo().getIdTorneo().equals(torneo.getIdTorneo())) {
            throw new IllegalArgumentException("La fecha no pertenece al torneo indicado");
        }

        List<Partido> partidosFecha = partidoRepository.findByFechaIdFecha(fecha.getIdFecha());
        for (Partido partidoExistente : partidosFecha) {
            if (partidoActualId != null && partidoActualId.equals(partidoExistente.getIdPartido())) {
                continue;
            }
            boolean participaLocal = partidoExistente.getEquipoLocal().getIdEquipo().equals(equipoLocal.getIdEquipo())
                    || partidoExistente.getEquipoVisitante().getIdEquipo().equals(equipoLocal.getIdEquipo());
            boolean participaVisitante = partidoExistente.getEquipoLocal().getIdEquipo().equals(equipoVisitante.getIdEquipo())
                    || partidoExistente.getEquipoVisitante().getIdEquipo().equals(equipoVisitante.getIdEquipo());
            if (participaLocal || participaVisitante) {
                throw new IllegalArgumentException("Uno de los equipos ya tiene un partido asignado en esta jornada");
            }
        }

        if (requestDTO.getGolesLocal() != null && requestDTO.getGolesLocal() < 0) {
            throw new IllegalArgumentException("Los goles del equipo local no pueden ser negativos");
        }

        if (requestDTO.getGolesVisitante() != null && requestDTO.getGolesVisitante() < 0) {
            throw new IllegalArgumentException("Los goles del equipo visitante no pueden ser negativos");
        }

        normalizarEstado(requestDTO.getEstado());
        if ("FINALIZADO".equalsIgnoreCase(requestDTO.getEstado())) {
            if (requestDTO.getGolesLocal() == null || requestDTO.getGolesVisitante() == null) {
                throw new IllegalArgumentException("Un partido finalizado debe tener goles local y visitante");
            }
        }
    }

    private PartidoResponseDTO convertirAResponseDTO(Partido partido) {
        PartidoResponseDTO dto = new PartidoResponseDTO(
                partido.getIdPartido(),
                partido.getFechaHora(),
                partido.getEstado(),
                partido.getGolesLocal(),
                partido.getGolesVisitante(),
                partido.getObservaciones(),
                partido.getCancha(),
                partido.getMvp() != null ? partido.getMvp().getIdJugador() : null,
                partido.getMvp() != null ? nombreJugador(partido.getMvp()) : null,
                partido.getMvp() != null ? partido.getMvp().getFoto() : null,
                partido.getTorneo().getIdTorneo(),
                partido.getTorneo().getNombre(),
                partido.getFecha().getIdFecha(),
                partido.getFecha().getNumeroFecha(),
                partido.getEquipoLocal().getIdEquipo(),
                partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getIdEquipo(),
                partido.getEquipoVisitante().getNombre()
        );
        dto.setPenalesLocal(partido.getPenalesLocal());
        dto.setPenalesVisitante(partido.getPenalesVisitante());
        return dto;
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarEstado(String estado) {
        String normalizado = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS.contains(normalizado)) throw new IllegalArgumentException("Estado de partido inválido");
        return normalizado;
    }

    private void validarTransicion(String actual, String siguiente, boolean desdeResultado) {
        String origen = normalizarEstado(actual);
        String destino = normalizarEstado(siguiente);
        if (origen.equals(destino)) return;
        if (desdeResultado && "FINALIZADO".equals(destino)
                && java.util.Set.of("PROGRAMADO", "EN_JUEGO").contains(origen)) return;
        if ("FINALIZADO".equals(destino) && !desdeResultado) {
            throw new IllegalArgumentException("Para finalizar el partido cargá el resultado");
        }
        boolean permitida = switch (origen) {
            case "PROGRAMADO" -> java.util.Set.of("EN_JUEGO", "SUSPENDIDO", "CANCELADO").contains(destino);
            case "EN_JUEGO" -> java.util.Set.of("SUSPENDIDO", "CANCELADO").contains(destino);
            case "SUSPENDIDO" -> java.util.Set.of("PROGRAMADO", "CANCELADO").contains(destino);
            case "FINALIZADO", "CANCELADO" -> false;
            default -> false;
        };
        if (!permitida) throw new IllegalArgumentException("No se puede pasar un partido de " + origen + " a " + destino);
    }

    @Transactional
    public PartidoResponseDTO cambiarEstado(Long id, EstadoPartidoRequestDTO request) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        authorization.verificarGestion(partido.getTorneo());
        String destino = normalizarEstado(request.estado());
        validarTransicion(partido.getEstado(), destino, false);
        if (java.util.Set.of("SUSPENDIDO", "CANCELADO").contains(destino)
                && (request.motivo() == null || request.motivo().isBlank())) {
            throw new IllegalArgumentException("Indicá el motivo del cambio");
        }
        partido.setEstado(destino);
        if (request.motivo() != null && !request.motivo().isBlank()) partido.setObservaciones(request.motivo().trim());
        return convertirAResponseDTO(partidoRepository.save(partido));
    }

    private String nombreJugador(com.futapp.model.Jugador jugador) {
        String apellido = jugador.getApellido() == null ? "" : " " + jugador.getApellido();
        return (jugador.getNombre() + apellido).trim();
    }

    @Transactional
    public PartidoResponseDTO elegirMvp(Long idPartido, MvpPartidoRequestDTO request) {
        Partido partido = partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + idPartido));
        authorization.verificarGestion(partido.getTorneo());
        if (!"FINALIZADO".equalsIgnoreCase(partido.getEstado())) {
            throw new IllegalArgumentException("Primero cargá el resultado para elegir el MVP");
        }
        if (request.idJugador() == null) {
            partido.setMvp(null);
            return convertirAResponseDTO(partidoRepository.save(partido));
        }
        var idsEquipo = java.util.List.of(partido.getEquipoLocal().getIdEquipo(), partido.getEquipoVisitante().getIdEquipo());
        if (!inscripcionesJugador.existsByJugadorIdJugadorAndInscripcionEquipoTorneoIdTorneoAndInscripcionEquipoEquipoIdEquipoInAndEstado(
                request.idJugador(), partido.getTorneo().getIdTorneo(), idsEquipo, "ACTIVA")) {
            throw new IllegalArgumentException("El MVP debe pertenecer al plantel de uno de los equipos del partido");
        }
        partido.setMvp(jugadores.findById(request.idJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado")));
        return convertirAResponseDTO(partidoRepository.save(partido));
    }

    public PartidoResponseDTO cargarResultado(Long id, ResultadoPartidoDTO dto) {

        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        authorization.verificarGestion(partido.getTorneo());

        if ("SUSPENDIDO".equalsIgnoreCase(partido.getEstado()) || "CANCELADO".equalsIgnoreCase(partido.getEstado())) {
            throw new IllegalArgumentException("Reprogramá el partido antes de cargar el resultado");
        }

        validarTransicion(partido.getEstado(), "FINALIZADO", true);

        if (dto.getGolesLocal() == null || dto.getGolesVisitante() == null) {
            throw new IllegalArgumentException("Debe informar ambos goles");
        }

        if (dto.getGolesLocal() < 0 || dto.getGolesVisitante() < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos");
        }

        boolean eliminatorio = esPartidoEliminatorio(partido);
        if (eliminatorio && dto.getGolesLocal().equals(dto.getGolesVisitante())) {
            if (dto.getPenalesLocal() == null || dto.getPenalesVisitante() == null) {
                throw new IllegalArgumentException("En un cruce empatado debés cargar la definición por penales");
            }
            if (dto.getPenalesLocal() < 0 || dto.getPenalesVisitante() < 0
                    || dto.getPenalesLocal().equals(dto.getPenalesVisitante())) {
                throw new IllegalArgumentException("La definición por penales debe tener un ganador");
            }
        }

        partido.setGolesLocal(dto.getGolesLocal());
        partido.setGolesVisitante(dto.getGolesVisitante());
        partido.setPenalesLocal(eliminatorio && dto.getGolesLocal().equals(dto.getGolesVisitante()) ? dto.getPenalesLocal() : null);
        partido.setPenalesVisitante(eliminatorio && dto.getGolesLocal().equals(dto.getGolesVisitante()) ? dto.getPenalesVisitante() : null);
        partido.setEstado("FINALIZADO");

        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, true);
        return convertirAResponseDTO(partido);
    }

    
}


