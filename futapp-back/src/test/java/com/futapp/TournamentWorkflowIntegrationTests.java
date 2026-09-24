package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TournamentWorkflowIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void completaElCircuitoPrincipalDeUnTorneo() throws Exception {
        String token = registrarOrganizador();

        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        JsonNode local = postJson("/equipos", token, equipo("Barrio Norte", "Azul", torneoId));
        JsonNode visitante = postJson("/equipos", token, equipo("Deportivo Sur", "Rojo", torneoId));

        putJson("/favoritos/torneos/" + torneoId, token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.favorito").value(true));
        putJson("/favoritos/equipos/" + local.get("idEquipo").asLong(), token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.favorito").value(true));
        mockMvc.perform(get("/favoritos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.torneos", org.hamcrest.Matchers.hasItem((int) torneoId)))
                .andExpect(jsonPath("$.equipos", org.hamcrest.Matchers.hasItem(local.get("idEquipo").asInt())));

        JsonNode jugadorLocal = postJson("/jugadores", token, jugador("Juan", "Norte", 10, local.get("idEquipo").asLong()));
        JsonNode jugadorVisitante = postJson("/jugadores", token, jugador("Pedro", "Sur", 9, visitante.get("idEquipo").asLong()));
        putJson("/favoritos/jugadores/" + jugadorLocal.get("idJugador").asLong(), token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.favorito").value(true));
        mockMvc.perform(get("/favoritos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores", org.hamcrest.Matchers.hasItem(jugadorLocal.get("idJugador").asInt())));
        postJson("/torneos/" + torneoId + "/visitas", null, null);
        postJson("/jugadores/" + jugadorLocal.get("idJugador").asLong() + "/visitas", null, null);
        mockMvc.perform(get("/torneos/" + torneoId)).andExpect(status().isOk()).andExpect(jsonPath("$.visitas").value(1));
        mockMvc.perform(get("/jugadores/" + jugadorLocal.get("idJugador").asLong())).andExpect(status().isOk()).andExpect(jsonPath("$.visitas").value(1));

        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05",
                "horaInicio", "14:00",
                "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60,
                "cancha", "Cancha principal"
        ));
        long partidoId = fixture.get(0).get("idPartido").asLong();

        putJson("/torneos/" + torneoId + "/iniciar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mockMvc.perform(get("/torneos/mios/panel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProximoPartido").value(partidoId))
                .andExpect(jsonPath("$[0].proximoPartidoLocal").value("Barrio Norte"))
                .andExpect(jsonPath("$[0].proximoPartidoVisitante").value("Deportivo Sur"));

        putJson("/partidos/" + partidoId + "/estado", token, Map.of("estado", "FINALIZADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Para finalizar el partido cargá el resultado"));

        putJson("/partidos/" + partidoId + "/estado", token, Map.of("estado", "EN_JUEGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        putJson("/partidos/" + partidoId + "/resultado", token,
                Map.of("golesLocal", 3, "golesVisitante", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.golesLocal").value(3))
                .andExpect(jsonPath("$.golesVisitante").value(1));

        mockMvc.perform(get("/partidos/{id}/jugadores", partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        postJson("/goles", token, Map.of("minuto", 12, "tipoGol", "Jugada", "idPartido", partidoId,
                "idJugador", jugadorLocal.get("idJugador").asLong(), "idEquipo", local.get("idEquipo").asLong()));
        postJson("/goles", token, Map.of("minuto", 22, "tipoGol", "Jugada", "idPartido", partidoId,
                "idJugador", jugadorLocal.get("idJugador").asLong(), "idEquipo", local.get("idEquipo").asLong()));
        postJson("/goles", token, Map.of("minuto", 52, "tipoGol", "Jugada", "idPartido", partidoId,
                "idJugador", jugadorLocal.get("idJugador").asLong(), "idEquipo", local.get("idEquipo").asLong()));

        mockMvc.perform(get("/goles/partido/{id}", partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].idPartido").value(partidoId))
                .andExpect(jsonPath("$[0].nombreJugador").value("Juan Norte"))
                .andExpect(jsonPath("$[0].nombreEquipo").value("Barrio Norte"));
        mockMvc.perform(get("/goles/jugador/{id}", jugadorLocal.get("idJugador").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].idJugador").value(jugadorLocal.get("idJugador").asLong()));

        mockMvc.perform(post("/goles").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("minuto", 70, "tipoGol", "Jugada",
                                "idPartido", partidoId, "idJugador", jugadorLocal.get("idJugador").asLong(),
                                "idEquipo", local.get("idEquipo").asLong()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No podés cargar más goles individuales que los indicados en el resultado"));

        putJson("/partidos/" + partidoId + "/resultado", token,
                Map.of("golesLocal", 2, "golesVisitante", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El resultado no puede ser menor que los goles individuales ya cargados"));
        JsonNode tarjeta = postJson("/tarjetas", token, Map.of("minuto", 31, "tipoTarjeta", "AMARILLA", "idPartido", partidoId,
                "idJugador", jugadorVisitante.get("idJugador").asLong(), "idEquipo", visitante.get("idEquipo").asLong()));

        mockMvc.perform(get("/tarjetas/partido/{id}", partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].tipoTarjeta").value("AMARILLA"))
                .andExpect(jsonPath("$[0].nombreJugador").value("Pedro Sur"))
                .andExpect(jsonPath("$[0].nombreEquipo").value("Deportivo Sur"));
        mockMvc.perform(get("/tarjetas/torneo/{id}", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        mockMvc.perform(put("/tarjetas/{id}", tarjeta.get("idTarjeta").asLong())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("minuto", 72, "tipoTarjeta", "ROJA",
                                "motivo", "Doble amarilla", "idPartido", partidoId,
                                "idJugador", jugadorVisitante.get("idJugador").asLong(),
                                "idEquipo", visitante.get("idEquipo").asLong()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoTarjeta").value("ROJA"))
                .andExpect(jsonPath("$.minuto").value(72));

        mockMvc.perform(delete("/partidos/{id}", partidoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede eliminar un partido que ya tiene resultado"));

        mockMvc.perform(post("/goles").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("minuto", 40, "tipoGol", "Jugada",
                                "idPartido", partidoId, "idJugador", jugadorLocal.get("idJugador").asLong(),
                                "idEquipo", visitante.get("idEquipo").asLong()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El jugador no integra el plantel activo de ese equipo en este torneo"));

        mockMvc.perform(get("/torneos/mios/panel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equiposInscriptos").value(2))
                .andExpect(jsonPath("$[0].equiposSinPlantel").value(0))
                .andExpect(jsonPath("$[0].partidosJugados").value(1))
                .andExpect(jsonPath("$[0].partidosPendientes").value(0))
                .andExpect(jsonPath("$[0].accionRecomendada").value("Finalizá el torneo"));

        mockMvc.perform(get("/torneos/{id}/tabla", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].puntos").value(3))
                .andExpect(jsonPath("$[0].partidosJugados").value(1))
                .andExpect(jsonPath("$[0].ganados").value(1))
                .andExpect(jsonPath("$[0].golesFavor").value(3))
                .andExpect(jsonPath("$[0].golesContra").value(1))
                .andExpect(jsonPath("$[1].puntos").value(0))
                .andExpect(jsonPath("$[1].perdidos").value(1));

        putJson("/torneos/" + torneoId + "/finalizar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.idCampeon").value(local.get("idEquipo").asLong()))
                .andExpect(jsonPath("$.nombreCampeon").value("Barrio Norte"));

        putJson("/partidos/" + partidoId + "/resultado", token,
                Map.of("golesLocal", 0, "golesVisitante", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El torneo está finalizado y su historial ya no se puede modificar"));
    }

    @Test
    void convierteAlJugadorEnOrganizadorAlCrearSuPrimerTorneo() throws Exception {
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Jugador organizador", "jugador-organizador-" + System.nanoTime() + "@test.local",
                                "password123", "JUGADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("JUGADOR"))
                .andReturn().getResponse().getContentAsString();
        String token = jsonMapper.readTree(response).get("token").asString();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("JUGADOR")));

        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ORGANIZADOR"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItems("JUGADOR", "ORGANIZADOR")));

        mockMvc.perform(get("/torneos/mios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idTorneo").value(torneoId));

        putJson("/torneos/" + torneoId, token, torneo())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Liga Integral"));
    }

    @Test
    void permiteEditarUnaDescripcionDeHastaQuinientosCaracteres() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        String descripcionLarga = "Torneo barrial con información detallada para equipos y jugadores. ".repeat(6).trim();
        request.put("descripcion", descripcionLarga);

        putJson("/torneos/" + torneoId, token, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion", org.hamcrest.Matchers.hasLength(descripcionLarga.length())))
                .andExpect(jsonPath("$.descripcion", org.hamcrest.Matchers.startsWith("Torneo Barrial")));
    }

    @Test
    void protegeElTorneoYLosEquiposDeUsuariosAjenos() throws Exception {
        String propietario = registrarUsuario("ORGANIZADOR");
        String otroOrganizador = registrarUsuario("ORGANIZADOR");
        String directorSinAprobar = registrarUsuario("DIRECTOR_EQUIPO");
        String jugador = registrarUsuario("JUGADOR");

        long torneoId = postJson("/torneos", propietario, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", propietario, equipo("Club protegido", "Azul", torneoId))
                .get("idEquipo").asLong();

        mockMvc.perform(get("/torneos/{id}", torneoId)).andExpect(status().isOk());

        putJson("/torneos/" + torneoId, otroOrganizador, torneo())
                .andExpect(status().isForbidden());
        putJson("/equipos/" + equipoId, directorSinAprobar, equipo("Intento director", "Rojo", torneoId))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/equipos").header("Authorization", "Bearer " + jugador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(equipo("Intento jugador", "Negro", torneoId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/equipos/{id}", equipoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Club Protegido"));
    }

    @Test
    void bloqueaCambiosDeRelacionMedianteIdsManipulados() throws Exception {
        String propietarioA = registrarUsuario("ORGANIZADOR");
        String propietarioB = registrarUsuario("ORGANIZADOR");
        long torneoA = postJson("/torneos", propietarioA, torneo()).get("idTorneo").asLong();
        long torneoB = postJson("/torneos", propietarioB, torneo()).get("idTorneo").asLong();
        long equipoA = postJson("/equipos", propietarioA, equipo("Equipo A", "Azul", torneoA))
                .get("idEquipo").asLong();
        long equipoB = postJson("/equipos", propietarioB, equipo("Equipo B", "Rojo", torneoB))
                .get("idEquipo").asLong();
        long jugadorA = postJson("/jugadores", propietarioA,
                jugador("Jugador", "Protegido", 7, equipoA)).get("idJugador").asLong();
        long fechaA = postJson("/fechas", propietarioA, Map.of(
                "numeroFecha", 1, "descripcion", "Fecha protegida", "estado", "PROGRAMADA", "idTorneo", torneoA))
                .get("idFecha").asLong();

        putJson("/equipos/" + equipoA, propietarioA, equipo("Equipo A", "Azul", torneoB))
                .andExpect(status().isBadRequest());
        putJson("/jugadores/" + jugadorA, propietarioA,
                jugador("Jugador", "Protegido", 7, equipoB))
                .andExpect(status().isBadRequest());
        putJson("/fechas/" + fechaA, propietarioA, Map.of(
                "numeroFecha", 1, "descripcion", "Intento de traslado", "estado", "PROGRAMADA", "idTorneo", torneoB))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/equipos/{id}", equipoA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idTorneo").value((int) torneoA));
        mockMvc.perform(get("/jugadores/{id}", jugadorA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idEquipo").value((int) equipoA));
        mockMvc.perform(get("/fechas/{id}", fechaA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idTorneo").value((int) torneoA));
    }

    @Test
    void rechazaMutacionesDePartidosYEstadisticasDeOtroOrganizador() throws Exception {
        String propietario = registrarUsuario("ORGANIZADOR");
        String atacante = registrarUsuario("ORGANIZADOR");
        long torneoId = postJson("/torneos", propietario, torneo()).get("idTorneo").asLong();
        JsonNode local = postJson("/equipos", propietario, equipo("Local seguro", "Azul", torneoId));
        JsonNode visitante = postJson("/equipos", propietario, equipo("Visitante seguro", "Rojo", torneoId));
        JsonNode jugador = postJson("/jugadores", propietario,
                jugador("Jugador", "Seguro", 10, local.get("idEquipo").asLong()));
        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", propietario, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha segura"));
        long partidoId = fixture.get(0).get("idPartido").asLong();

        putJson("/partidos/" + partidoId + "/resultado", atacante,
                Map.of("golesLocal", 1, "golesVisitante", 0)).andExpect(status().isForbidden());
        putJson("/partidos/" + partidoId + "/estado", atacante,
                Map.of("estado", "EN_JUEGO")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/partidos/{id}", partidoId)
                        .header("Authorization", "Bearer " + atacante))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/goles").header("Authorization", "Bearer " + atacante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "minuto", 10, "tipoGol", "Jugada", "idPartido", partidoId,
                                "idJugador", jugador.get("idJugador").asLong(),
                                "idEquipo", local.get("idEquipo").asLong()))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/tarjetas").header("Authorization", "Bearer " + atacante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "minuto", 20, "tipoTarjeta", "AMARILLA", "motivo", "Falta",
                                "idPartido", partidoId, "idJugador", jugador.get("idJugador").asLong(),
                                "idEquipo", local.get("idEquipo").asLong()))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/jugadores/{id}", jugador.get("idJugador").asLong())
                        .header("Authorization", "Bearer " + atacante))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/equipos/{id}", visitante.get("idEquipo").asLong())
                        .header("Authorization", "Bearer " + atacante))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/partidos/{id}", partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$.golesLocal").value(0))
                .andExpect(jsonPath("$.golesVisitante").value(0));
    }

    @Test
    void permiteAlOrganizadorAprobarUnJugadorSiElEquipoNoTieneDirector() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String cuentaJugador = registrarUsuario("JUGADOR");
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador, equipo("Equipo sin director", "Azul", torneoId))
                .get("idEquipo").asLong();
        long jugadorId = postJson("/jugadores", organizador,
                jugador("Jugador", "Reclamado", 5, equipoId)).get("idJugador").asLong();
        long otroJugadorId = postJson("/jugadores", organizador,
                jugador("Otro", "Jugador", 6, equipoId)).get("idJugador").asLong();

        JsonNode solicitud = postJson("/reclamos/jugadores/" + jugadorId, cuentaJugador,
                Map.of("idTorneo", torneoId, "mensaje", "Este perfil soy yo"));
        putJson("/reclamos/" + solicitud.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idJugador").value((int) jugadorId));

        String segundoReclamo = mockMvc.perform(post("/reclamos/jugadores/{id}", otroJugadorId)
                        .header("Authorization", "Bearer " + cuentaJugador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "idTorneo", torneoId, "mensaje", "Solicito cambiar mi perfil"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long segundaSolicitud = jsonMapper.readTree(segundoReclamo).get("idSolicitud").asLong();
        putJson("/reclamos/" + segundaSolicitud + "/aprobar", organizador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idJugador").value((int) otroJugadorId));
    }

    @Test
    void creaLaEstructuraDeZonasYPlayoffsConCuposVacios() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Zonas + Playoffs");
        request.put("cantidadEquipos", 8);
        request.put("cantidadZonas", 2);
        request.put("clasificadosPorZona", 2);
        request.put("generarEquiposPrueba", true);

        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();

        mockMvc.perform(get("/torneos/{id}/estructura", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.divisiones", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.divisiones[0].nombre").value("División Única"))
                .andExpect(jsonPath("$.divisiones[0].fases", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.divisiones[0].fases[0].tipo").value("ZONAS"))
                .andExpect(jsonPath("$.divisiones[0].fases[0].zonas", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.divisiones[0].fases[1].tipo").value("ELIMINACION_DIRECTA"))
                .andExpect(jsonPath("$.divisiones[0].fases[1].clasificados").value(4))
                .andExpect(jsonPath("$.divisiones[0].cupos", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$.divisiones[0].cupos[0].etiqueta").value("Equipo 1"))
                .andExpect(jsonPath("$.divisiones[0].cupos[0].equipo").value("Equipo 1"))
                .andExpect(jsonPath("$.divisiones[0].cupos[7].etiqueta").value("Equipo 8"));

        JsonNode partidosZona = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        mockMvc.perform(get("/partidos/torneo/{id}", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(12)))
                .andExpect(jsonPath("$[0].observaciones", org.hamcrest.Matchers.startsWith("Zona ")));
        for (JsonNode partido : partidosZona) putJson("/partidos/" + partido.get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 2, "golesVisitante", 1)).andExpect(status().isOk());
        JsonNode playoffs = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(2, playoffs.size());
        org.junit.jupiter.api.Assertions.assertEquals("Semifinales", playoffs.get(0).get("observaciones").asString());
    }

    @Test
    void generaLaPrimeraLlaveDeEliminacionDirecta() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Eliminación directa");
        request.put("cantidadEquipos", 8);
        request.put("generarEquiposPrueba", true);
        JsonNode torneoCreado = postJson("/torneos", token, request);
        long torneoId = torneoCreado.get("idTorneo").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(0, torneoCreado.get("puntosVictoria").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(0, torneoCreado.get("puntosEmpate").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(0, torneoCreado.get("puntosDerrota").asInt());
        org.junit.jupiter.api.Assertions.assertEquals("PENALES", torneoCreado.get("criterioDesempate").asString());

        JsonNode partidos = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        org.junit.jupiter.api.Assertions.assertEquals(4, partidos.size());
        org.junit.jupiter.api.Assertions.assertEquals("Cuartos de final", partidos.get(0).get("observaciones").asString());
        int indice = 0;
        for (JsonNode partido : partidos) {
            putJson("/partidos/" + partido.get("idPartido").asLong() + "/resultado", token,
                    indice++ == 0 ? Map.of("golesLocal", 1, "golesVisitante", 1, "penalesLocal", 5, "penalesVisitante", 4)
                            : Map.of("golesLocal", 2, "golesVisitante", 1)).andExpect(status().isOk());
        }
        JsonNode semifinales = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(2, semifinales.size());
        org.junit.jupiter.api.Assertions.assertEquals("Semifinales", semifinales.get(0).get("observaciones").asString());
        org.junit.jupiter.api.Assertions.assertEquals("Equipo 1", semifinales.get(0).get("nombreEquipoLocal").asString());
        org.junit.jupiter.api.Assertions.assertEquals("Equipo 2", semifinales.get(0).get("nombreEquipoVisitante").asString());
        org.junit.jupiter.api.Assertions.assertEquals("Equipo 3", semifinales.get(1).get("nombreEquipoLocal").asString());
        org.junit.jupiter.api.Assertions.assertEquals("Equipo 4", semifinales.get(1).get("nombreEquipoVisitante").asString());
        putJson("/partidos/" + partidos.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 0, "golesVisitante", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No podés cambiar el ganador porque la siguiente ronda ya fue generada"));
        for (JsonNode semifinal : semifinales) {
            putJson("/partidos/" + semifinal.get("idPartido").asLong() + "/resultado", token,
                    Map.of("golesLocal", 2, "golesVisitante", 0)).andExpect(status().isOk());
        }
        JsonNode finalPartidos = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(1, finalPartidos.size());
        JsonNode finalPartido = finalPartidos.get(0);
        putJson("/partidos/" + finalPartido.get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 3, "golesVisitante", 1)).andExpect(status().isOk());
        putJson("/torneos/" + torneoId + "/finalizar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCampeon").value(finalPartido.get("nombreEquipoLocal").asString()));
    }

    @Test
    void generaPlayoffsDesdeLaTablaDeLiga() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Liga + Playoffs"); request.put("cantidadEquipos", 4);
        request.put("generarEquiposPrueba", true); request.put("jugadoresPorEquipoPrueba", 2);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        JsonNode liga = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        for (JsonNode partido : liga) {
            putJson("/partidos/" + partido.get("idPartido").asLong() + "/resultado", token,
                    Map.of("golesLocal", 2, "golesVisitante", 1)).andExpect(status().isOk());
        }
        JsonNode playoffs = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(2, playoffs.size());
        org.junit.jupiter.api.Assertions.assertEquals("Semifinales", playoffs.get(0).get("observaciones").asString());
        putJson("/partidos/" + liga.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 3, "golesVisitante", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No podés modificar la fase clasificatoria después de generar los playoffs"));
    }

    @Test
    void permiteCerrarUnaLigaConPartidosFinalizadosYCancelados() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("cantidadEquipos", 2);
        request.put("generarEquiposPrueba", true);
        request.put("jugadoresPorEquipoPrueba", 1);
        request.put("idaVuelta", true);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        org.junit.jupiter.api.Assertions.assertEquals(2, fixture.size());

        putJson("/partidos/" + fixture.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 2, "golesVisitante", 0)).andExpect(status().isOk());
        putJson("/partidos/" + fixture.get(1).get("idPartido").asLong() + "/estado", token,
                Map.of("estado", "CANCELADO", "motivo", "No se disputará"))
                .andExpect(status().isOk());

        putJson("/fechas/" + fixture.get(0).get("idFecha").asLong() + "/cerrar", token, null)
                .andExpect(status().isOk());
        putJson("/fechas/" + fixture.get(1).get("idFecha").asLong() + "/cerrar", token, null)
                .andExpect(status().isOk());
        putJson("/torneos/" + torneoId + "/finalizar", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.idCampeon").isNumber());

        mockMvc.perform(get("/torneos/pagina").param("cantidad", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.cantidad").value(1)).andExpect(jsonPath("$.totalElementos").isNumber());
        mockMvc.perform(get("/partidos/torneo/{id}/pagina", torneoId).param("cantidad", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.totalElementos").value(2));
        mockMvc.perform(get("/equipos/pagina").param("cantidad", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)));
        mockMvc.perform(get("/jugadores/pagina").param("cantidad", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void conservaPasesLibresConSeisEquipos() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Eliminación directa"); request.put("cantidadEquipos", 6);
        request.put("generarEquiposPrueba", true);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        JsonNode preliminar = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        org.junit.jupiter.api.Assertions.assertEquals(2, preliminar.size());
        for (JsonNode partido : preliminar) putJson("/partidos/" + partido.get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 1, "golesVisitante", 0)).andExpect(status().isOk());
        JsonNode semis = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(2, semis.size());
    }

    @Test
    void publicaYSimulaUnTorneoConAuditoriaEIncidencias() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("cantidadEquipos", 4); request.put("generarEquiposPrueba", true);
        request.put("jugadoresPorEquipoPrueba", 3);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        putJson("/torneos/" + torneoId + "/publicar", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PUBLICADO"));
        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        JsonNode simulados = postJson("/partidos/torneo/" + torneoId + "/simular", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(6, simulados.size());
        for (JsonNode partido : simulados) {
            org.junit.jupiter.api.Assertions.assertEquals("FINALIZADO", partido.get("estado").asString());
            org.junit.jupiter.api.Assertions.assertFalse(partido.get("nombreMvp").isNull());
        }
        mockMvc.perform(get("/partidos/{id}/historial-resultado", fixture.get(0).get("idPartido").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].golesLocalNuevo").value(2));
    }

    private String registrarOrganizador() throws Exception {
        return registrarUsuario("ORGANIZADOR");
    }

    private String registrarUsuario(String rol) throws Exception {
        String email = "workflow-" + System.nanoTime() + "@test.local";
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Usuario Workflow", email, "password123", rol))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("token").asString();
    }

    private JsonNode postJson(String path, String token, Object body) throws Exception {
        String response = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(response);
    }

    private org.springframework.test.web.servlet.ResultActions putJson(String path, String token, Object body)
            throws Exception {
        var request = put(path).header("Authorization", "Bearer " + token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request);
    }

    private Map<String, Object> torneo() {
        Map<String, Object> torneo = new LinkedHashMap<>();
        torneo.put("nombre", "Liga integral");
        torneo.put("descripcion", "Torneo creado por la prueba del circuito principal");
        torneo.put("ubicacion", "Buenos Aires");
        torneo.put("estado", "BORRADOR");
        torneo.put("formato", "LIGA");
        torneo.put("cantidadEquipos", 2);
        torneo.put("generarEquiposPrueba", false);
        return torneo;
    }

    private Map<String, Object> equipo(String nombre, String color, long torneoId) {
        Map<String, Object> equipo = new LinkedHashMap<>();
        equipo.put("nombre", nombre);
        equipo.put("colorCamiseta", color);
        equipo.put("barrio", nombre);
        equipo.put("responsable", "Responsable " + nombre);
        equipo.put("telefonoContacto", "1100000000");
        equipo.put("estado", "ACTIVO");
        equipo.put("idTorneo", torneoId);
        return equipo;
    }

    private Map<String, Object> jugador(String nombre, String apellido, int dorsal, long equipoId) {
        Map<String, Object> jugador = new LinkedHashMap<>();
        jugador.put("nombre", nombre);
        jugador.put("apellido", apellido);
        jugador.put("fechaNacimiento", "2000-01-01");
        jugador.put("numeroCamiseta", dorsal);
        jugador.put("posicion", "Delantero");
        jugador.put("estado", "ACTIVO");
        jugador.put("idEquipo", equipoId);
        return jugador;
    }
}
