package com.futapp.service;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.dto.InscripcionEquipoRequestDTO;
import com.futapp.dto.InscripcionJugadorRequestDTO;
import com.futapp.dto.ActualizacionInscripcionJugadorDTO;
import com.futapp.dto.BajaEquipoTorneoRequestDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.InscripcionJugador;
import com.futapp.model.Equipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.CupoEquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDate;
import com.futapp.model.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional(readOnly = true)
public class InscripcionService {
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;
    private final TorneoRepository torneos;
    private final TorneoAuthorizationService authorization;
    private final EquipoAuthorizationService equipoAuthorization;
    private final PartidoRepository partidos;
    private final TorneoVisibilityService visibility;
    private final CupoEquipoRepository cupos;

    public InscripcionService(InscripcionEquipoRepository inscripcionesEquipo,
                              InscripcionJugadorRepository inscripcionesJugador,
                              EquipoRepository equipos, JugadorRepository jugadores,
                              TorneoRepository torneos, TorneoAuthorizationService authorization,
                              EquipoAuthorizationService equipoAuthorization,
                              PartidoRepository partidos, TorneoVisibilityService visibility,
                              CupoEquipoRepository cupos) {
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.torneos = torneos;
        this.authorization = authorization;
        this.equipoAuthorization = equipoAuthorization;
        this.partidos = partidos;
        this.visibility = visibility;
        this.cupos = cupos;
    }

    public List<InscripcionEquipoResponseDTO> historialEquipo(Long idEquipo) {
        Equipo equipo = equipos.findById(idEquipo)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + idEquipo));
        if (!visibility.puedeVer(equipo.getTorneo()) && !equipoAuthorization.puedeGestionar(equipo)) {
            throw new ResourceNotFoundException("Equipo no encontrado");
        }
        return inscripcionesEquipo.findByEquipoIdEquipoOrderByFechaAltaDesc(idEquipo)
                .stream().filter(item -> puedeVer(item, equipo)).map(this::equipoDto).toList();
    }

    public List<InscripcionJugadorResponseDTO> plantel(Long idInscripcion) {
        InscripcionEquipo inscripcion = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        if (!puedeVer(inscripcion, inscripcion.getEquipo())) throw new ResourceNotFoundException("Inscripción no encontrada");
        return inscripcionesJugador.findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(idInscripcion)
                .stream().map(this::jugadorDto).toList();
    }

    public List<InscripcionJugadorResponseDTO> historialJugador(Long idJugador) {
        var jugador = jugadores.findById(idJugador)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + idJugador));
        boolean propio = esJugadorActual(jugador.getIdJugador());
        if (!visibility.puedeVer(jugador.getEquipo().getTorneo())
                && !equipoAuthorization.puedeGestionar(jugador.getEquipo()) && !propio) {
            throw new ResourceNotFoundException("Jugador no encontrado");
        }
        return inscripcionesJugador.findByJugadorIdJugadorOrderByFechaAltaDesc(idJugador)
                .stream().filter(item -> puedeVer(item.getInscripcionEquipo(), item.getInscripcionEquipo().getEquipo())
                        || propio).map(this::jugadorDto).toList();
    }

    @Transactional
    public InscripcionEquipoResponseDTO inscribirEquipo(InscripcionEquipoRequestDTO request) {
        authorization.verificarGestion(request.idTorneo());
        Equipo equipo = equipos.findById(request.idEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + request.idEquipo()));
        equipoAuthorization.verificarGestion(equipo);
        return inscribirEquipoInterno(request.idEquipo(), request.idTorneo(), request.categoria());
    }

    @Transactional
    public InscripcionEquipoResponseDTO inscribirEquipoAprobado(Long idEquipo, Long idTorneo, String categoria) {
        return inscribirEquipoInterno(idEquipo, idTorneo, categoria);
    }

    private InscripcionEquipoResponseDTO inscribirEquipoInterno(Long idEquipo, Long idTorneo, String categoria) {
        Equipo equipo = equipos.findById(idEquipo)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + idEquipo));
        var torneo = torneos.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo));
        if (inscripcionesEquipo.existsByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, idTorneo)) {
            throw new IllegalArgumentException("El club ya está inscrito en este torneo");
        }
        if (!partidos.findByTorneoIdTorneo(idTorneo).isEmpty()) {
            throw new IllegalArgumentException("No podés inscribir otro equipo después de generar el fixture. Rehacé el fixture antes de agregarlo");
        }
        var activas = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(idTorneo, "ACTIVA");
        int capacidad = torneo.getCantidadEquipos() == null ? 2 : torneo.getCantidadEquipos();
        InscripcionEquipo reemplazo = activas.stream()
                .filter(item -> esEquipoProvisorio(item.getEquipo()))
                .findFirst().orElse(null);
        if (activas.size() >= capacidad && reemplazo == null) {
            throw new IllegalArgumentException("El torneo ya tiene los " + capacidad + " equipos configurados");
        }
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        inscripcion.setCategoria(normalizar(categoria));
        inscripcion.setEstado("ACTIVA");
        inscripcion = inscripcionesEquipo.save(inscripcion);
        if (reemplazo != null) {
            Equipo placeholder = reemplazo.getEquipo();
            cupos.findByDivisionTorneoIdTorneoOrderByNumero(idTorneo).stream()
                    .filter(cupo -> cupo.getEquipo() != null && cupo.getEquipo().getIdEquipo().equals(placeholder.getIdEquipo()))
                    .findFirst().ifPresent(cupo -> { cupo.setEquipo(equipo); cupos.save(cupo); });
            inscripcionesJugador.deleteByInscripcionEquipoIdInscripcion(reemplazo.getIdInscripcion());
            inscripcionesEquipo.delete(reemplazo);
            jugadores.deleteAll(jugadores.findByEquipoIdEquipo(placeholder.getIdEquipo()));
            equipos.delete(placeholder);
        }
        return equipoDto(inscripcion);
    }

    @Transactional
    public void quitarEquipoDelTorneo(Long idTorneo, Long idEquipo) {
        authorization.verificarGestion(idTorneo);
        InscripcionEquipo inscripcion = inscripcionesEquipo
                .findByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("El equipo no está inscripto en este torneo"));
        if (partidos.existsByTorneoIdTorneoAndEquipoLocalIdEquipoOrTorneoIdTorneoAndEquipoVisitanteIdEquipo(
                idTorneo, idEquipo, idTorneo, idEquipo)) {
            throw new IllegalArgumentException("No se puede quitar el equipo porque ya tiene partidos en este torneo");
        }
        inscripcionesJugador.deleteByInscripcionEquipoIdInscripcion(inscripcion.getIdInscripcion());
        inscripcionesEquipo.delete(inscripcion);
    }

    @Transactional
    public InscripcionEquipoResponseDTO darDeBajaEquipoDelTorneo(Long idTorneo, Long idEquipo,
                                                                  BajaEquipoTorneoRequestDTO request) {
        authorization.verificarGestion(idTorneo);
        InscripcionEquipo inscripcion = inscripcionesEquipo
                .findByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("El equipo no está inscripto en este torneo"));
        if (!"ACTIVA".equalsIgnoreCase(inscripcion.getEstado())) {
            throw new IllegalArgumentException("El equipo ya está dado de baja en este torneo");
        }

        String resolucion = request.resolucionPartidos().trim().toUpperCase();
        if (!java.util.Set.of("WALKOVER", "CANCELAR").contains(resolucion)) {
            throw new IllegalArgumentException("La resolución debe ser WALKOVER o CANCELAR");
        }

        String motivo = normalizar(request.motivo());
        String detalle = "Baja de " + inscripcion.getEquipo().getNombre()
                + (motivo == null ? "" : ": " + motivo);
        for (var partido : partidos.findByTorneoIdTorneo(idTorneo)) {
            boolean esLocal = partido.getEquipoLocal().getIdEquipo().equals(idEquipo);
            boolean esVisitante = partido.getEquipoVisitante().getIdEquipo().equals(idEquipo);
            if ((!esLocal && !esVisitante) || "FINALIZADO".equalsIgnoreCase(partido.getEstado())
                    || "CANCELADO".equalsIgnoreCase(partido.getEstado())) continue;

            partido.setMvp(null);
            partido.setPenalesLocal(null);
            partido.setPenalesVisitante(null);
            partido.setObservaciones(detalle);
            if ("WALKOVER".equals(resolucion)) {
                partido.setEstado("FINALIZADO");
                partido.setGolesLocal(esLocal ? 0 : 3);
                partido.setGolesVisitante(esVisitante ? 0 : 3);
            } else {
                partido.setEstado("CANCELADO");
                partido.setGolesLocal(null);
                partido.setGolesVisitante(null);
            }
            partidos.save(partido);
        }

        LocalDate hoy = LocalDate.now();
        inscripcion.setEstado("BAJA");
        inscripcion.setFechaBaja(hoy);
        for (var jugador : inscripcionesJugador
                .findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(inscripcion.getIdInscripcion())) {
            if (!"ACTIVA".equalsIgnoreCase(jugador.getEstado())) continue;
            jugador.setEstado("INACTIVA");
            jugador.setFechaBaja(hoy);
            inscripcionesJugador.save(jugador);
        }
        return equipoDto(inscripcionesEquipo.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO inscribirJugador(Long idInscripcion,
                                                           InscripcionJugadorRequestDTO request) {
        var inscripcionEquipo = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        equipoAuthorization.verificarGestionPlantel(inscripcionEquipo);
        verificarInscripcionEquipoActiva(inscripcionEquipo);
        var jugador = jugadores.findById(request.idJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + request.idJugador()));
        if (jugador.getEquipo() == null || !jugador.getEquipo().getIdEquipo().equals(inscripcionEquipo.getEquipo().getIdEquipo())) {
            throw new IllegalArgumentException("Solo podés convocar jugadores que pertenezcan a este club");
        }
        if ("INACTIVO".equalsIgnoreCase(jugador.getEstado())) {
            throw new IllegalArgumentException("No podés convocar un jugador inactivo");
        }
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndJugadorIdJugador(idInscripcion, request.idJugador())) {
            throw new IllegalArgumentException("El jugador ya pertenece a este plantel");
        }
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstado(idInscripcion, request.numeroCamiseta(), "ACTIVA")) {
            throw new IllegalArgumentException("Ya existe un jugador con ese número en este plantel");
        }
        InscripcionJugador inscripcion = new InscripcionJugador();
        inscripcion.setInscripcionEquipo(inscripcionEquipo);
        inscripcion.setJugador(jugador);
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()) == null ? jugador.getPosicion() : request.posicion().trim());
        inscripcion.setEstado("ACTIVA");
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    @Transactional
    public List<InscripcionJugadorResponseDTO> generarJugadoresDePrueba(Long idInscripcion, int cantidad) {
        if (cantidad < 1 || cantidad > 30) {
            throw new IllegalArgumentException("Podés generar entre 1 y 30 jugadores por vez");
        }
        var inscripcionEquipo = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        equipoAuthorization.verificarGestionPlantel(inscripcionEquipo);
        verificarInscripcionEquipoActiva(inscripcionEquipo);
        String estadoTorneo = inscripcionEquipo.getTorneo().getEstado();
        if (!"BORRADOR".equalsIgnoreCase(estadoTorneo) && !"PUBLICADO".equalsIgnoreCase(estadoTorneo)) {
            throw new IllegalArgumentException("Los jugadores de prueba solo se pueden generar antes de iniciar el torneo");
        }

        var actuales = inscripcionesJugador.findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(idInscripcion);
        if (actuales.size() + cantidad > 30) {
            throw new IllegalArgumentException("El plantel no puede superar los 30 jugadores");
        }
        var dorsalesUsados = new HashSet<Integer>();
        actuales.stream().filter(item -> "ACTIVA".equalsIgnoreCase(item.getEstado()))
                .map(InscripcionJugador::getNumeroCamiseta).filter(java.util.Objects::nonNull)
                .forEach(dorsalesUsados::add);

        String[] nombres = {"Mateo", "Santiago", "Thiago", "Benjamín", "Bautista", "Joaquín", "Lautaro", "Tomás", "Franco", "Agustín", "Nicolás", "Valentín", "Facundo", "Lucas", "Bruno", "Martín", "Juan", "Ramiro", "Gonzalo", "Emiliano"};
        String[] apellidos = {"González", "Rodríguez", "Fernández", "López", "Martínez", "García", "Pérez", "Sánchez", "Romero", "Díaz", "Torres", "Álvarez", "Ruiz", "Ramírez", "Acosta", "Benítez", "Medina", "Herrera", "Suárez", "Castro"};
        String[] posiciones = {"Arquero", "Defensor", "Defensor", "Mediocampista", "Mediocampista", "Delantero"};
        var random = ThreadLocalRandom.current();
        var generados = new java.util.ArrayList<InscripcionJugadorResponseDTO>();
        for (int indice = 0; indice < cantidad; indice++) {
            int dorsal = 1;
            while (dorsalesUsados.contains(dorsal) && dorsal <= 99) dorsal++;
            if (dorsal > 99) throw new IllegalArgumentException("No quedan dorsales disponibles en el plantel");
            dorsalesUsados.add(dorsal);

            var jugador = new com.futapp.model.Jugador();
            jugador.setNombre(nombres[random.nextInt(nombres.length)]);
            jugador.setApellido(apellidos[random.nextInt(apellidos.length)]);
            jugador.setFechaNacimiento(LocalDate.of(random.nextInt(1990, 2008), random.nextInt(1, 13), random.nextInt(1, 28)).toString());
            jugador.setNumeroCamiseta(dorsal);
            jugador.setPosicion(posiciones[indice % posiciones.length]);
            jugador.setPiernaHabil(random.nextBoolean() ? "Derecha" : "Izquierda");
            jugador.setEstado("ACTIVO");
            jugador.setEquipo(inscripcionEquipo.getEquipo());
            jugador = jugadores.save(jugador);

            var alta = new InscripcionJugador();
            alta.setInscripcionEquipo(inscripcionEquipo);
            alta.setJugador(jugador);
            alta.setNumeroCamiseta(dorsal);
            alta.setPosicion(jugador.getPosicion());
            alta.setEstado("ACTIVA");
            generados.add(jugadorDto(inscripcionesJugador.save(alta)));
        }
        return generados;
    }

    @Transactional
    public List<InscripcionJugadorResponseDTO> cargarJugadoresPorNombre(Long idInscripcion, List<String> nombresCrudos) {
        var inscripcionEquipo = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        equipoAuthorization.verificarGestionPlantel(inscripcionEquipo);
        verificarInscripcionEquipoActiva(inscripcionEquipo);
        String estadoTorneo = inscripcionEquipo.getTorneo().getEstado();
        if (!"BORRADOR".equalsIgnoreCase(estadoTorneo) && !"PUBLICADO".equalsIgnoreCase(estadoTorneo)) {
            throw new IllegalArgumentException("La carga rápida de jugadores solo está disponible antes de iniciar el torneo");
        }
        var nombres = nombresCrudos.stream().map(value -> value == null ? "" : value.trim().replaceAll("\\s+", " "))
                .filter(value -> !value.isBlank()).toList();
        if (nombres.isEmpty()) throw new IllegalArgumentException("Ingresá al menos un jugador");
        if (nombres.stream().map(String::toLowerCase).distinct().count() != nombres.size()) {
            throw new IllegalArgumentException("La lista contiene jugadores repetidos");
        }
        var actuales = inscripcionesJugador.findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(idInscripcion);
        var placeholders = actuales.stream().filter(item -> "Prueba".equalsIgnoreCase(item.getJugador().getApellido())).iterator();
        long reales = actuales.stream().filter(item -> !"Prueba".equalsIgnoreCase(item.getJugador().getApellido())).count();
        if (reales + nombres.size() > 30) {
            throw new IllegalArgumentException("Solo quedan " + Math.max(0, 30 - reales) + " lugares disponibles en el plantel");
        }
        var dorsalesUsados = new HashSet<Integer>();
        actuales.stream().map(InscripcionJugador::getNumeroCamiseta).filter(java.util.Objects::nonNull).forEach(dorsalesUsados::add);
        var resultado = new java.util.ArrayList<InscripcionJugadorResponseDTO>();
        for (int indice = 0; indice < nombres.size(); indice++) {
            String[] partes = nombres.get(indice).split(" ", 2);
            String nombre = partes[0]; String apellido = partes.length > 1 ? partes[1] : "A completar";
            if (placeholders.hasNext()) {
                var alta = placeholders.next(); var jugador = alta.getJugador();
                jugador.setNombre(nombre); jugador.setApellido(apellido); jugador.setPosicion(null); jugadores.save(jugador);
                alta.setPosicion(null); inscripcionesJugador.save(alta);
                resultado.add(jugadorDto(alta));
            } else {
                int dorsal = 1; while (dorsalesUsados.contains(dorsal) && dorsal <= 99) dorsal++;
                if (dorsal > 99) throw new IllegalArgumentException("No quedan dorsales disponibles en el plantel");
                dorsalesUsados.add(dorsal);
                var jugador = new com.futapp.model.Jugador();
                jugador.setNombre(nombre); jugador.setApellido(apellido); jugador.setFechaNacimiento("2000-01-01");
                jugador.setNumeroCamiseta(dorsal); jugador.setPosicion(null);
                jugador.setEstado("ACTIVO"); jugador.setEquipo(inscripcionEquipo.getEquipo()); jugador = jugadores.save(jugador);
                var alta = new InscripcionJugador(); alta.setInscripcionEquipo(inscripcionEquipo); alta.setJugador(jugador);
                alta.setNumeroCamiseta(dorsal); alta.setPosicion(null); alta.setEstado("ACTIVA");
                resultado.add(jugadorDto(inscripcionesJugador.save(alta)));
            }
        }
        while (placeholders.hasNext()) {
            var altaSobrante = placeholders.next();
            var jugadorSobrante = altaSobrante.getJugador();
            inscripcionesJugador.delete(altaSobrante);
            jugadores.delete(jugadorSobrante);
        }
        return resultado;
    }

    @Transactional
    public InscripcionJugadorResponseDTO actualizarJugador(Long id, ActualizacionInscripcionJugadorDTO request) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        validarDorsalDisponible(inscripcion, request.numeroCamiseta());
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()));
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO darDeBaja(Long id) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        if ("INACTIVA".equals(inscripcion.getEstado())) throw new IllegalArgumentException("El jugador ya está dado de baja en este plantel");
        inscripcion.setEstado("INACTIVA");
        inscripcion.setFechaBaja(LocalDate.now());
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO reactivarJugador(Long id, ActualizacionInscripcionJugadorDTO request) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        verificarInscripcionEquipoActiva(inscripcion.getInscripcionEquipo());
        validarDorsalDisponible(inscripcion, request.numeroCamiseta());
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()));
        inscripcion.setEstado("ACTIVA");
        inscripcion.setFechaBaja(null);
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    private InscripcionJugador obtenerInscripcionJugador(Long id) {
        return inscripcionesJugador.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción de jugador no encontrada con id: " + id));
    }

    private void validarDorsalDisponible(InscripcionJugador inscripcion, Integer dorsal) {
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstadoAndIdInscripcionJugadorNot(
                inscripcion.getInscripcionEquipo().getIdInscripcion(), dorsal, "ACTIVA", inscripcion.getIdInscripcionJugador())) {
            throw new IllegalArgumentException("Ya existe un jugador activo con ese número en este plantel");
        }
    }

    private void verificarInscripcionEquipoActiva(InscripcionEquipo inscripcion) {
        if (!"ACTIVA".equalsIgnoreCase(inscripcion.getEstado())) {
            throw new IllegalArgumentException("El equipo está dado de baja en este torneo");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private boolean esEquipoProvisorio(Equipo equipo) {
        return equipo.getDescripcion() != null
                && equipo.getDescripcion().toLowerCase(java.util.Locale.ROOT).startsWith("equipo de prueba generado");
    }

    private boolean puedeVer(InscripcionEquipo inscripcion, Equipo equipo) {
        return visibility.puedeVer(inscripcion.getTorneo()) || equipoAuthorization.puedeGestionar(equipo);
    }

    private boolean esJugadorActual(Long idJugador) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) return false;
        return jugadores.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .map(item -> item.getIdJugador().equals(idJugador)).orElse(false);
    }

    private InscripcionEquipoResponseDTO equipoDto(InscripcionEquipo item) {
        return new InscripcionEquipoResponseDTO(
                item.getIdInscripcion(), item.getEquipo().getIdEquipo(), item.getEquipo().getNombre(),
                item.getEquipo().getCodigoPublico(), item.getTorneo().getIdTorneo(),
                item.getTorneo().getNombre(), item.getTorneo().getCodigoPublico(), item.getCategoria(),
                item.getEstado(), item.getFechaAlta(), item.getFechaBaja(),
                inscripcionesJugador.countByInscripcionEquipoIdInscripcionAndEstado(item.getIdInscripcion(), "ACTIVA")
        );
    }

    private InscripcionJugadorResponseDTO jugadorDto(InscripcionJugador item) {
        var inscripcion = item.getInscripcionEquipo();
        var jugador = item.getJugador();
        return new InscripcionJugadorResponseDTO(
                item.getIdInscripcionJugador(), inscripcion.getIdInscripcion(), jugador.getIdJugador(),
                jugador.getNombre() + " " + jugador.getApellido(), jugador.getCodigoPublico(), jugador.getFoto(),
                item.getNumeroCamiseta(), item.getPosicion(), item.getEstado(), item.getFechaAlta(),
                item.getFechaBaja(), inscripcion.getEquipo().getIdEquipo(), inscripcion.getEquipo().getNombre(),
                inscripcion.getEquipo().getEscudo(),
                inscripcion.getTorneo().getIdTorneo(), inscripcion.getTorneo().getNombre()
        );
    }
}
