package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.futapp.repository.TorneoRepository;
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
    @Autowired private TorneoRepository torneoRepository;

    @Test
    void cargaEquiposPorListaEnUnTorneoVacio() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = torneo();
        request.put("cantidadEquipos", 8);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();

        postJson("/equipos/torneo/" + torneoId + "/lote", token, Map.of("nombres", java.util.List.of(
                "Boca Juniors", "Newells", "Racing", "Lobos", "Velez", "Suiza", "Cero Quimica", "Turcos"
        )));

        mockMvc.perform(get("/equipos/torneo/{id}", torneoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$[0].nombre").value("Boca Juniors"));
    }

    @Test
    void cargaRapidaCreaEquiposYPlantelesEnUnaSolaOperacion() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = torneo();
        request.put("cantidadEquipos", 4);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();

        JsonNode carga = postJson("/equipos/torneo/" + torneoId + "/carga-rapida", token, Map.of(
                "nombresEquipos", java.util.List.of("Los Pibes", "Racing"),
                "jugadoresPorEquipo", 3));

        org.junit.jupiter.api.Assertions.assertEquals(2, carga.get("equipos").size());
        org.junit.jupiter.api.Assertions.assertEquals(6, carga.get("jugadoresCreados").asInt());
        for (JsonNode equipo : carga.get("equipos")) {
            String historial = mockMvc.perform(get("/inscripciones/equipos/{id}", equipo.get("idEquipo").asLong())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            long inscripcionId = jsonMapper.readTree(historial).get(0).get("idInscripcion").asLong();
            mockMvc.perform(get("/inscripciones/{id}/jugadores", inscripcionId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
                    .andExpect(jsonPath("$[0].posicion").value(org.hamcrest.Matchers.nullValue()));
        }
    }

    @Test
    void cargaRapidaInvalidaNoCreaEquiposParciales() throws Exception {
        String token = registrarOrganizador();
        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();

        mockMvc.perform(post("/equipos/torneo/{id}/carga-rapida", torneoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "nombresEquipos", java.util.List.of("Racing", "racing"),
                                "jugadoresPorEquipo", 5))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/equipos/torneo/{id}", torneoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void cargaRapidaReemplazaCuposProvisoriosYRespetaLaCantidadElegida() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = torneo();
        request.put("generarEquiposPrueba", true);
        request.put("jugadoresPorEquipoPrueba", 5);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();

        JsonNode carga = postJson("/equipos/torneo/" + torneoId + "/carga-rapida", token, Map.of(
                "nombresEquipos", java.util.List.of("Barrio Uno", "Barrio Dos"),
                "jugadoresPorEquipo", 3));

        for (JsonNode equipo : carga.get("equipos")) {
            String historial = mockMvc.perform(get("/inscripciones/equipos/{id}", equipo.get("idEquipo").asLong())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            long inscripcionId = jsonMapper.readTree(historial).get(0).get("idInscripcion").asLong();
            mockMvc.perform(get("/inscripciones/{id}/jugadores", inscripcionId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
                    .andExpect(jsonPath("$[*].nombreJugador", org.hamcrest.Matchers.contains(
                            "Jugador 1", "Jugador 2", "Jugador 3")));
        }
    }

    @Test
    void completaElCircuitoPrincipalDeUnTorneo() throws Exception {
        String token = registrarOrganizador();

        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        mockMvc.perform(get("/torneos/{id}", torneoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/torneos/{id}", torneoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/torneos/{id}/estructura", torneoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/torneos/{id}/tabla", torneoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/torneos/{id}/estructura", torneoId)
                        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
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
        mockMvc.perform(get("/inscripciones/equipos/{id}", local.get("idEquipo").asLong()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/inscripciones/equipos/{id}", local.get("idEquipo").asLong())
                        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mockMvc.perform(get("/sanciones/torneo/{id}", torneoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/sanciones/torneo/{id}", torneoId)
                        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        for (int indice = 2; indice <= 5; indice++) {
            postJson("/jugadores", token, jugador("Local" + indice, "Norte", 10 + indice, local.get("idEquipo").asLong()));
            postJson("/jugadores", token, jugador("Visitante" + indice, "Sur", 20 + indice, visitante.get("idEquipo").asLong()));
        }
        putJson("/favoritos/jugadores/" + jugadorLocal.get("idJugador").asLong(), token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.favorito").value(true));
        mockMvc.perform(get("/favoritos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores", org.hamcrest.Matchers.hasItem(jugadorLocal.get("idJugador").asInt())));
        postJson("/torneos/" + torneoId + "/visitas", null, null);
        postJson("/jugadores/" + jugadorLocal.get("idJugador").asLong() + "/visitas", null, null);
        mockMvc.perform(get("/torneos/" + torneoId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.visitas").value(1));
        mockMvc.perform(get("/jugadores/" + jugadorLocal.get("idJugador").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.visitas").value(1));

        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05",
                "horaInicio", "14:00",
                "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60,
                "cancha", "Cancha principal"
        ));
        long partidoId = fixture.get(0).get("idPartido").asLong();
        mockMvc.perform(get("/partidos/{id}", partidoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/fechas/torneo/{id}", torneoId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/partidos/{id}", partidoId)
                        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mockMvc.perform(get("/fechas/torneo/{id}", torneoId)
                        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        putJson("/torneos/" + torneoId + "/iniciar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mockMvc.perform(get("/jugadores/{id}", jugadorLocal.get("idJugador").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.fechaNacimiento").value(org.hamcrest.Matchers.nullValue()));
        mockMvc.perform(get("/jugadores/{id}", jugadorLocal.get("idJugador").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaNacimiento").value("2000-01-01"));
        mockMvc.perform(get("/equipos/{id}", local.get("idEquipo").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.telefonoContacto").value(org.hamcrest.Matchers.nullValue()));

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
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(10)));

        postJson("/goles", token, Map.of("minuto", 12, "tipoGol", "Jugada", "idPartido", partidoId,
                "idJugador", jugadorLocal.get("idJugador").asLong(), "idEquipo", local.get("idEquipo").asLong()));
        postJson("/goles", token, Map.of("tipoGol", "Jugada", "idPartido", partidoId,
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
                        .content(jsonMapper.writeValueAsString(Map.of("minuto", 72, "tipoTarjeta", "ROJA", "fechasSuspension", 2,
                                "motivo", "Doble amarilla", "idPartido", partidoId,
                                "idJugador", jugadorVisitante.get("idJugador").asLong(),
                                "idEquipo", visitante.get("idEquipo").asLong()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoTarjeta").value("ROJA"))
                .andExpect(jsonPath("$.minuto").value(72));

        String sancionesJson = mockMvc.perform(get("/sanciones/torneo/{id}", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].fechasTotal").value(2))
                .andExpect(jsonPath("$[0].fechasCumplidas").value(0))
                .andExpect(jsonPath("$[0].fechasRestantes").value(2))
                .andExpect(jsonPath("$[0].activa").value(true))
                .andReturn().getResponse().getContentAsString();
        long sancionId = jsonMapper.readTree(sancionesJson).get(0).get("idSancion").asLong();
        putJson("/sanciones/" + sancionId, token, Map.of("fechasTotal", 3, "motivo", "Conducta grave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechasTotal").value(3))
                .andExpect(jsonPath("$.motivo").value("Conducta grave"));

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
    void unaRojaBloqueaAlJugadorEnLasFechasSiguientes() throws Exception {
        String token = registrarOrganizador();
        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        long local = postJson("/equipos", token, equipo("Local disciplinario", "Azul", torneoId)).get("idEquipo").asLong();
        long visitante = postJson("/equipos", token, equipo("Visitante disciplinario", "Rojo", torneoId)).get("idEquipo").asLong();
        long expulsado = postJson("/jugadores", token, jugador("Jugador", "Expulsado", 9, local)).get("idJugador").asLong();

        long fechaUno = postJson("/fechas", token, Map.of("numeroFecha", 1, "descripcion", "Fecha 1", "estado", "PROGRAMADA", "idTorneo", torneoId)).get("idFecha").asLong();
        long partidoUno = postJson("/partidos", token, Map.of("fechaHora", "2026-09-05T14:00:00", "estado", "PROGRAMADO",
                "idTorneo", torneoId, "idFecha", fechaUno, "idEquipoLocal", local, "idEquipoVisitante", visitante)).get("idPartido").asLong();
        putJson("/partidos/" + partidoUno + "/resultado", token, Map.of("golesLocal", 0, "golesVisitante", 0)).andExpect(status().isOk());
        postJson("/tarjetas", token, Map.of("tipoTarjeta", "ROJA", "fechasSuspension", 2, "idPartido", partidoUno,
                "idJugador", expulsado, "idEquipo", local));

        long fechaDos = postJson("/fechas", token, Map.of("numeroFecha", 2, "descripcion", "Fecha 2", "estado", "PROGRAMADA", "idTorneo", torneoId)).get("idFecha").asLong();
        long partidoDos = postJson("/partidos", token, Map.of("fechaHora", "2026-09-12T14:00:00", "estado", "PROGRAMADO",
                "idTorneo", torneoId, "idFecha", fechaDos, "idEquipoLocal", local, "idEquipoVisitante", visitante)).get("idPartido").asLong();
        putJson("/partidos/" + partidoDos + "/resultado", token, Map.of("golesLocal", 1, "golesVisitante", 0)).andExpect(status().isOk());

        mockMvc.perform(get("/partidos/{id}/jugadores", partidoDos).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idJugador == " + expulsado + ")].suspendido")
                        .value(org.hamcrest.Matchers.contains(true)));
        mockMvc.perform(post("/goles").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("tipoGol", "Jugada", "idPartido", partidoDos,
                                "idJugador", expulsado, "idEquipo", local))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El jugador está suspendido para esta fecha y no puede participar"));
        putJson("/partidos/" + partidoDos + "/mvp", token, Map.of("idJugador", expulsado))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El jugador está suspendido para esta fecha y no puede ser elegido MVP"));
    }

    @Test
    void exigeCuposPlantelesYPrimeraFechaParaIniciar() throws Exception {
        String token = registrarOrganizador();
        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        JsonNode local = postJson("/equipos", token, equipo("Local incompleto", "Azul", torneoId));

        putJson("/torneos/" + torneoId + "/iniciar", token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "deben estar inscriptos los 2 equipos")));

        JsonNode visitante = postJson("/equipos", token, equipo("Visitante incompleto", "Rojo", torneoId));
        putJson("/torneos/" + torneoId + "/iniciar", token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "al menos 5 jugadores activos")));

        for (int indice = 1; indice <= 5; indice++) {
            postJson("/jugadores", token, jugador("Local" + indice, "Prueba", 10 + indice, local.get("idEquipo").asLong()));
            postJson("/jugadores", token, jugador("Visitante" + indice, "Prueba", 20 + indice, visitante.get("idEquipo").asLong()));
        }
        putJson("/torneos/" + torneoId + "/iniciar", token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Creá la primera fecha antes de iniciar el torneo"));
    }

    @Test
    void eliminaBorradoresCancelaPublicadosYArchivaFinalizados() throws Exception {
        String token = registrarOrganizador();
        long borradorId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        mockMvc.perform(delete("/torneos/{id}", borradorId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/torneos/mios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.empty()));

        long publicadoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();
        putJson("/torneos/" + publicadoId + "/publicar", token, null).andExpect(status().isOk());
        putJson("/torneos/" + publicadoId + "/cancelar", token, Map.of("motivo", "Falta de participantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoCancelacion").value("Falta de participantes"));
        mockMvc.perform(get("/torneos")).andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.empty()));

        var torneoFinalizado = torneoRepository.findById(publicadoId).orElseThrow();
        torneoFinalizado.setEstado("FINALIZADO");
        torneoRepository.saveAndFlush(torneoFinalizado);
        putJson("/torneos/" + publicadoId + "/archivar", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ARCHIVADO"));
        putJson("/torneos/" + publicadoId + "/restaurar", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    void crearTorneoNoCambiaElRolDeUnaCuentaJugador() throws Exception {
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

        mockMvc.perform(post("/torneos")
                        .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(torneo())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Debés crear una cuenta de organizador de torneos para realizar esta acción"));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("JUGADOR"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.contains("JUGADOR")));
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

        mockMvc.perform(get("/torneos/{id}", torneoId)
                        .header("Authorization", "Bearer " + propietario))
                .andExpect(status().isOk());

        putJson("/torneos/" + torneoId, otroOrganizador, torneo())
                .andExpect(status().isForbidden());
        putJson("/equipos/" + equipoId, directorSinAprobar, equipo("Intento director", "Rojo", torneoId))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/equipos").header("Authorization", "Bearer " + jugador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(equipo("Intento jugador", "Negro", torneoId))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/equipos/{id}", equipoId)
                        .header("Authorization", "Bearer " + propietario))
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

        mockMvc.perform(get("/equipos/{id}", equipoA)
                        .header("Authorization", "Bearer " + propietarioA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idTorneo").value((int) torneoA));
        mockMvc.perform(get("/jugadores/{id}", jugadorA)
                        .header("Authorization", "Bearer " + propietarioA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idEquipo").value((int) equipoA));
        mockMvc.perform(get("/fechas/{id}", fechaA)
                        .header("Authorization", "Bearer " + propietarioA))
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

        mockMvc.perform(get("/partidos/{id}", partidoId)
                        .header("Authorization", "Bearer " + propietario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$.golesLocal").value(0))
                .andExpect(jsonPath("$.golesVisitante").value(0));
    }

    @Test
    void soloElDirectorDelEquipoPuedeAprobarYUnificarUnJugador() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String director = registrarUsuario("DIRECTOR_EQUIPO");
        String cuentaJugador = registrarUsuario("JUGADOR");
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador, equipo("Equipo sin director", "Azul", torneoId))
                .get("idEquipo").asLong();
        Map<String, Object> jugadorConFoto = jugador("Jugador", "Reclamado", 5, equipoId);
        jugadorConFoto.put("foto", "/uploads/foto-oficial.png");
        long jugadorId = postJson("/jugadores", organizador, jugadorConFoto).get("idJugador").asLong();
        JsonNode otroJugador = postJson("/jugadores", organizador,
                jugador("Otro", "Jugador", 6, equipoId));
        long otroJugadorId = otroJugador.get("idJugador").asLong();
        String codigoDuplicado = otroJugador.get("codigoPublico").asText();

        JsonNode reclamoEquipo = postJson("/reclamos/equipos/" + equipoId, director,
                Map.of("idTorneo", torneoId, "mensaje", "Soy el dueño del equipo"));
        putJson("/reclamos/" + reclamoEquipo.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk());

        JsonNode solicitud = postJson("/reclamos/jugadores/" + jugadorId, cuentaJugador,
                Map.of("idTorneo", torneoId, "mensaje", "Este perfil soy yo"));
        putJson("/reclamos/" + solicitud.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isForbidden());
        putJson("/reclamos/" + solicitud.get("idSolicitud").asLong() + "/aprobar", director, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idJugador").value((int) jugadorId));
        putJson("/perfil-jugador/mio", cuentaJugador, Map.of("foto", "/uploads/foto-no-autorizada.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foto").value("/uploads/foto-oficial.png"));

        String segundoReclamo = mockMvc.perform(post("/reclamos/jugadores/{id}", otroJugadorId)
                        .header("Authorization", "Bearer " + cuentaJugador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "idTorneo", torneoId, "mensaje", "Solicito cambiar mi perfil"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long segundaSolicitud = jsonMapper.readTree(segundoReclamo).get("idSolicitud").asLong();
        putJson("/reclamos/" + segundaSolicitud + "/aprobar", director, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.idJugador").value((int) jugadorId));

        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idJugador").value((int) jugadorId));
        mockMvc.perform(get("/jugadores/{id}", otroJugadorId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/jugadores/codigo/{codigo}", codigoDuplicado)
                        .header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idJugador").value((int) jugadorId));
    }

    @Test
    void organizadorPuedeRevisarJugadorCuandoElEquipoNoTieneDirector() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String cuentaJugador = registrarUsuario("JUGADOR");
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador, equipo("Equipo sin director", "Azul", torneoId))
                .get("idEquipo").asLong();
        long jugadorId = postJson("/jugadores", organizador,
                jugador("Jugador", "Sin director", 5, equipoId)).get("idJugador").asLong();

        JsonNode solicitud = postJson("/reclamos/jugadores/" + jugadorId, cuentaJugador,
                Map.of("idTorneo", torneoId, "mensaje", "Este perfil soy yo"));

        mockMvc.perform(get("/reclamos/pendientes")
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idSolicitud").value(solicitud.get("idSolicitud").asInt()));
        putJson("/reclamos/" + solicitud.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    void solicitudesYNotificacionesLleganSoloAlResponsableCorrecto() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String director = registrarUsuario("DIRECTOR_EQUIPO");
        String cuentaJugador = registrarUsuario("JUGADOR");
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador,
                equipo("Equipo de notificaciones", "Azul", torneoId)).get("idEquipo").asLong();
        long jugadorId = postJson("/jugadores", organizador,
                jugador("Jugador", "Notificado", 5, equipoId)).get("idJugador").asLong();

        JsonNode reclamoEquipo = postJson("/reclamos/equipos/" + equipoId, director,
                Map.of("idTorneo", torneoId, "mensaje", "Administro este equipo"));
        mockMvc.perform(get("/notificaciones/no-leidas/count").header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(1));
        mockMvc.perform(get("/notificaciones").header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Nueva solicitud pendiente"))
                .andExpect(jsonPath("$[0].url").value("/solicitudes"));
        mockMvc.perform(get("/reclamos/pendientes").header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        putJson("/reclamos/" + reclamoEquipo.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk());
        mockMvc.perform(get("/notificaciones/no-leidas/count").header("Authorization", "Bearer " + director))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(1));
        putJson("/notificaciones/leer-todas", organizador, null).andExpect(status().isOk());
        putJson("/notificaciones/leer-todas", director, null).andExpect(status().isOk());

        JsonNode reclamoJugador = postJson("/reclamos/jugadores/" + jugadorId, cuentaJugador,
                Map.of("idTorneo", torneoId, "mensaje", "Este perfil soy yo"));
        mockMvc.perform(get("/notificaciones/no-leidas/count").header("Authorization", "Bearer " + director))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(1));
        mockMvc.perform(get("/notificaciones/no-leidas/count").header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(0));
        mockMvc.perform(get("/reclamos/pendientes").header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idSolicitud").value(reclamoJugador.get("idSolicitud").asInt()));
        mockMvc.perform(get("/reclamos/pendientes").header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        putJson("/reclamos/" + reclamoJugador.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isForbidden());
        putJson("/reclamos/" + reclamoJugador.get("idSolicitud").asLong() + "/aprobar", director, null)
                .andExpect(status().isOk());

        String notificacionesJugador = mockMvc.perform(get("/notificaciones")
                        .header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Solicitud aprobada"))
                .andReturn().getResponse().getContentAsString();
        long idNotificacionJugador = jsonMapper.readTree(notificacionesJugador).get(0).get("idNotificacion").asLong();
        putJson("/notificaciones/" + idNotificacionJugador + "/leer", organizador, null)
                .andExpect(status().isForbidden());
        putJson("/notificaciones/" + idNotificacionJugador + "/leer", cuentaJugador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void emailDeContactoInvitaAlDirectorYUnDirectorConClubRecibeSolicitudDeFusion() throws Exception {
        String organizador = registrarOrganizador();
        String emailDirector = "director-email-" + System.nanoTime() + "@test.local";
        String registro = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Director Email", emailDirector, "password123", "DIRECTOR_EQUIPO"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String director = jsonMapper.readTree(registro).get("token").asString();

        long torneoUno = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        Map<String, Object> clubUno = equipo("Club oficial", "Azul", torneoUno);
        clubUno.put("emailContacto", emailDirector);
        long clubUnoId = postJson("/equipos", organizador, clubUno).get("idEquipo").asLong();

        String invitacion = mockMvc.perform(get("/reclamos/pendientes")
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("VINCULACION_EQUIPO"))
                .andExpect(jsonPath("$[0].idEquipo").value((int) clubUnoId))
                .andReturn().getResponse().getContentAsString();
        long invitacionId = jsonMapper.readTree(invitacion).get(0).get("idSolicitud").asLong();
        putJson("/reclamos/" + invitacionId + "/aprobar", director, null).andExpect(status().isOk());
        mockMvc.perform(get("/equipos/mios").header("Authorization", "Bearer " + director))
                .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].idEquipo").value((int) clubUnoId));

        long torneoDos = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        Map<String, Object> clubDuplicado = equipo("Club duplicado", "Rojo", torneoDos);
        clubDuplicado.put("emailContacto", emailDirector);
        JsonNode duplicado = postJson("/equipos", organizador, clubDuplicado);
        long clubDuplicadoId = duplicado.get("idEquipo").asLong();
        String codigoDuplicado = duplicado.get("codigoPublico").asString();
        long jugadorDuplicadoId = postJson("/jugadores", organizador,
                jugador("Jugador", "Del duplicado", 10, clubDuplicadoId)).get("idJugador").asLong();
        String solicitudFusion = mockMvc.perform(get("/reclamos/pendientes")
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("FUSION_EQUIPO"))
                .andExpect(jsonPath("$[0].idEquipo").value((int) clubDuplicadoId))
                .andExpect(jsonPath("$[0].idEquipoDestino").value((int) clubUnoId))
                .andReturn().getResponse().getContentAsString();
        long solicitudFusionId = jsonMapper.readTree(solicitudFusion).get(0).get("idSolicitud").asLong();

        putJson("/reclamos/" + solicitudFusionId + "/aprobar", director, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
        mockMvc.perform(get("/equipos/{id}", clubDuplicadoId)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/equipos/codigo/{codigo}", codigoDuplicado)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEquipo").value((int) clubUnoId));
        mockMvc.perform(get("/inscripciones/equipos/{id}", clubUnoId)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[*].idTorneo", org.hamcrest.Matchers.containsInAnyOrder(
                        (int) torneoUno, (int) torneoDos)));
        mockMvc.perform(get("/jugadores/{id}", jugadorDuplicadoId)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEquipo").value((int) clubUnoId));
    }

    @Test
    void impideMoverUnJugadorDesdeLaEdicionYConservaSuHistorial() throws Exception {
        String organizador = registrarOrganizador();
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoOrigen = postJson("/equipos", organizador,
                equipo("Equipo origen", "Azul", torneoId)).get("idEquipo").asLong();
        long equipoDestino = postJson("/equipos", organizador,
                equipo("Equipo destino", "Rojo", torneoId)).get("idEquipo").asLong();
        long jugadorId = postJson("/jugadores", organizador,
                jugador("Jugador", "Sin transferencia", 8, equipoOrigen)).get("idJugador").asLong();

        mockMvc.perform(put("/jugadores/{id}", jugadorId)
                        .header("Authorization", "Bearer " + organizador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                jugador("Jugador", "Sin transferencia", 8, equipoDestino))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Un jugador no se puede mover a otro equipo desde la edición"));

        mockMvc.perform(get("/jugadores/{id}", jugadorId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEquipo").value((int) equipoOrigen));
        mockMvc.perform(get("/inscripciones/jugadores/{id}", jugadorId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].idEquipo").value((int) equipoOrigen))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    void directorAprobadoEsElUnicoGestorDelPerfilDelEquipo() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String otroOrganizador = registrarUsuario("ORGANIZADOR");
        String director = registrarUsuario("DIRECTOR_EQUIPO");
        String otroDirector = registrarUsuario("DIRECTOR_EQUIPO");

        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long otroTorneoId = postJson("/torneos", otroOrganizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador,
                equipo("Club con dueño", "Azul", torneoId)).get("idEquipo").asLong();
        long equipoAjenoId = postJson("/equipos", otroOrganizador,
                equipo("Club ajeno", "Rojo", otroTorneoId)).get("idEquipo").asLong();

        JsonNode reclamo = postJson("/reclamos/equipos/" + equipoId, director,
                Map.of("idTorneo", torneoId, "mensaje", "Administro este equipo"));
        putJson("/reclamos/" + reclamo.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk());

        putJson("/equipos/" + equipoId, director,
                equipo("Club administrado", "Verde", torneoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Club Administrado"));

        putJson("/equipos/" + equipoId, organizador,
                equipo("Cambio del organizador", "Negro", torneoId))
                .andExpect(status().isForbidden());
        putJson("/equipos/" + equipoId, otroDirector,
                equipo("Cambio de otro director", "Negro", torneoId))
                .andExpect(status().isForbidden());
        putJson("/equipos/" + equipoAjenoId, director,
                equipo("Cambio sobre club ajeno", "Negro", otroTorneoId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/equipos/{id}/permisos", equipoId)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk()).andExpect(jsonPath("$.puedeGestionar").value(true));
        mockMvc.perform(get("/equipos/{id}/permisos", equipoId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.puedeGestionar").value(false));
        mockMvc.perform(get("/equipos/{id}", equipoId)
                        .header("Authorization", "Bearer " + director))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Club Administrado"));
    }

    @Test
    void cambiosPrivadosDeJugadorRequierenAprobacionDelJugador() throws Exception {
        String organizador = registrarUsuario("ORGANIZADOR");
        String director = registrarUsuario("DIRECTOR_EQUIPO");
        String cuentaJugador = registrarUsuario("JUGADOR");
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoId = postJson("/equipos", organizador, equipo("Equipo con jugador verificado", "Azul", torneoId))
                .get("idEquipo").asLong();

        Map<String, Object> original = jugador("Nombre original", "Apellido original", 5, equipoId);
        original.put("dni", "30111222");
        original.put("foto", "/uploads/foto-original.png");
        long jugadorId = postJson("/jugadores", organizador, original).get("idJugador").asLong();

        JsonNode reclamoEquipo = postJson("/reclamos/equipos/" + equipoId, director,
                Map.of("idTorneo", torneoId, "mensaje", "Administro el equipo"));
        putJson("/reclamos/" + reclamoEquipo.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isOk());
        JsonNode reclamoJugador = postJson("/reclamos/jugadores/" + jugadorId, cuentaJugador,
                Map.of("idTorneo", torneoId, "mensaje", "Este jugador soy yo"));
        putJson("/reclamos/" + reclamoJugador.get("idSolicitud").asLong() + "/aprobar", director, null)
                .andExpect(status().isOk());

        Map<String, Object> edicionDeportiva = jugador("Nombre corregido", "Apellido corregido", 8, equipoId);
        edicionDeportiva.put("dni", "40999888");
        edicionDeportiva.put("fechaNacimiento", "2001-02-03");
        edicionDeportiva.put("foto", "/uploads/foto-equipo.png");
        putJson("/jugadores/" + jugadorId, director, edicionDeportiva)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Original"))
                .andExpect(jsonPath("$.apellido").value("Apellido Original"))
                .andExpect(jsonPath("$.dni").value("30111222"))
                .andExpect(jsonPath("$.fechaNacimiento").value("2000-01-01"))
                .andExpect(jsonPath("$.numeroCamiseta").value(8))
                .andExpect(jsonPath("$.foto").value("/uploads/foto-equipo.png"));

        JsonNode cambio = postJson("/reclamos/jugadores/" + jugadorId + "/cambios", director,
                Map.of("idTorneo", torneoId, "idEquipo", equipoId,
                        "nombre", "Nombre corregido", "apellido", "Apellido corregido",
                        "dni", "40999888", "fechaNacimiento", "2001-02-03",
                        "mensaje", "Corrección del registro oficial"));

        putJson("/reclamos/" + cambio.get("idSolicitud").asLong() + "/aprobar", organizador, null)
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/reclamos/pendientes")
                        .header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_JUGADOR"))
                .andExpect(jsonPath("$[0].nombrePropuesto").value("Nombre Corregido"))
                .andExpect(jsonPath("$[0].nombreEquipo").value("Equipo Con Jugador Verificado"));
        putJson("/reclamos/" + cambio.get("idSolicitud").asLong() + "/aprobar", cuentaJugador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        mockMvc.perform(get("/jugadores/{id}", jugadorId)
                        .header("Authorization", "Bearer " + cuentaJugador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Corregido"))
                .andExpect(jsonPath("$.apellido").value("Apellido Corregido"))
                .andExpect(jsonPath("$.dni").value("40999888"))
                .andExpect(jsonPath("$.fechaNacimiento").value("2001-02-03"));
    }

    @Test
    void soloUnaCuentaOrganizadoraPuedeCrearTorneos() throws Exception {
        String jugador = registrarUsuario("JUGADOR");
        String director = registrarUsuario("DIRECTOR_EQUIPO");
        String organizador = registrarUsuario("ORGANIZADOR");

        mockMvc.perform(post("/torneos")
                        .header("Authorization", "Bearer " + jugador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(torneo())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/torneos")
                        .header("Authorization", "Bearer " + director)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(torneo())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/torneos")
                        .header("Authorization", "Bearer " + organizador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(torneo())))
                .andExpect(status().isOk());
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

        mockMvc.perform(get("/torneos/{id}/estructura", torneoId)
                        .header("Authorization", "Bearer " + token))
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
        mockMvc.perform(get("/partidos/torneo/{id}", torneoId)
                        .header("Authorization", "Bearer " + token))
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
    void respetaLaCantidadElegidaDeClasificadosEnLigaConPlayoffs() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Liga + Playoffs");
        request.put("cantidadEquipos", 8);
        request.put("clasificadosPlayoff", 4);
        request.put("generarEquiposPrueba", false);

        JsonNode creado = postJson("/torneos", token, request);
        long torneoId = creado.get("idTorneo").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(4, creado.get("clasificadosPlayoff").asInt());

        mockMvc.perform(get("/torneos/{id}/estructura", torneoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.divisiones[0].fases[1].clasificados").value(4))
                .andExpect(jsonPath("$.divisiones[0].cupos", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$.divisiones[0].cupos[0].idEquipo").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void rechazaUnaClasificacionQueNoFormaUnaLlaveValida() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Zonas + Playoffs");
        request.put("cantidadEquipos", 12);
        request.put("cantidadZonas", 3);
        request.put("clasificadosPorZona", 2);

        mockMvc.perform(post("/torneos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La clasificación debe formar una llave de 2, 4, 8 o 16 equipos"));
    }

    @Test
    void reconstruyeUnaEstructuraVaciaPeroProtegeLaQueYaTieneEquipos() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();

        request.put("cantidadEquipos", 6);
        putJson("/torneos/" + torneoId, token, request)
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidadEquipos").value(6));
        mockMvc.perform(get("/torneos/{id}/estructura", torneoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.divisiones[0].cupos", org.hamcrest.Matchers.hasSize(6)));

        postJson("/equipos", token, equipo("Club real", "Azul", torneoId));
        request.put("cantidadEquipos", 8);
        putJson("/torneos/" + torneoId, token, request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quitá los equipos inscriptos antes de reconstruir la estructura del torneo"));
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
    void unaLlaveNoAvanzaConPartidosSuspendidosNiEmpatesSinPenales() throws Exception {
        String token = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("formato", "Eliminación directa");
        request.put("cantidadEquipos", 4);
        request.put("generarEquiposPrueba", true);
        long torneoId = postJson("/torneos", token, request).get("idTorneo").asLong();
        JsonNode semifinales = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", token, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));

        putJson("/partidos/" + semifinales.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 1, "golesVisitante", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("penales")));
        putJson("/partidos/" + semifinales.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 1, "golesVisitante", 1, "penalesLocal", 4, "penalesVisitante", 3))
                .andExpect(status().isOk());
        putJson("/partidos/" + semifinales.get(1).get("idPartido").asLong() + "/estado", token,
                Map.of("estado", "SUSPENDIDO", "motivo", "Lluvia"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/partidos/torneo/{id}/generar-siguiente-llave", torneoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cargá todos los resultados de la ronda antes de generar la siguiente"));

        putJson("/partidos/" + semifinales.get(1).get("idPartido").asLong() + "/estado", token,
                Map.of("estado", "PROGRAMADO", "motivo", "Reprogramado"))
                .andExpect(status().isOk());
        putJson("/partidos/" + semifinales.get(1).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 2, "golesVisitante", 0)).andExpect(status().isOk());
        JsonNode finalPartidos = postJson("/partidos/torneo/" + torneoId + "/generar-siguiente-llave", token, null);
        org.junit.jupiter.api.Assertions.assertEquals(1, finalPartidos.size());
        putJson("/partidos/" + finalPartidos.get(0).get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 0, "golesVisitante", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("penales")));
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
    void unaJornadaSuspendidaExplicaElPartidoPendienteYNoActivaLaSiguiente() throws Exception {
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
        JsonNode primerPartido = fixture.get(0);
        JsonNode segundoPartido = fixture.get(1);

        putJson("/partidos/" + primerPartido.get("idPartido").asLong() + "/estado", token,
                Map.of("estado", "SUSPENDIDO", "motivo", "Lluvia"))
                .andExpect(status().isOk());
        putJson("/fechas/" + primerPartido.get("idFecha").asLong() + "/cerrar", token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Equipo 1 vs Equipo 2")))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("suspendido")));
        mockMvc.perform(get("/fechas/{id}", segundoPartido.get("idFecha").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        putJson("/partidos/" + primerPartido.get("idPartido").asLong() + "/estado", token,
                Map.of("estado", "PROGRAMADO", "motivo", "Reprogramado"))
                .andExpect(status().isOk());
        putJson("/partidos/" + primerPartido.get("idPartido").asLong() + "/resultado", token,
                Map.of("golesLocal", 1, "golesVisitante", 0)).andExpect(status().isOk());
        putJson("/fechas/" + primerPartido.get("idFecha").asLong() + "/cerrar", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("FINALIZADA"));
        mockMvc.perform(get("/fechas/{id}", segundoPartido.get("idFecha").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
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

    @Test
    void alInscribirUnClubEnOtroTorneoElDirectorEligeElPlantel() throws Exception {
        String organizador = registrarOrganizador();
        long torneoOrigen = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long club = postJson("/equipos", organizador, equipo("Club convocable", "Azul", torneoOrigen))
                .get("idEquipo").asLong();
        long otroClub = postJson("/equipos", organizador, equipo("Club rival", "Rojo", torneoOrigen))
                .get("idEquipo").asLong();
        long jugadorClub = postJson("/jugadores", organizador, jugador("Jugador", "Del club", 7, club))
                .get("idJugador").asLong();
        long jugadorAjeno = postJson("/jugadores", organizador, jugador("Jugador", "Ajeno", 9, otroClub))
                .get("idJugador").asLong();

        long torneoDestino = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        JsonNode inscripcion = postJson("/inscripciones/equipos", organizador,
                Map.of("idEquipo", club, "idTorneo", torneoDestino, "categoria", "Primera"));
        long idInscripcion = inscripcion.get("idInscripcion").asLong();

        mockMvc.perform(get("/inscripciones/{id}/jugadores", idInscripcion)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        postJson("/inscripciones/" + idInscripcion + "/jugadores", organizador,
                Map.of("idJugador", jugadorClub, "numeroCamiseta", 7, "posicion", "Delantero"));
        mockMvc.perform(get("/inscripciones/{id}/jugadores", idInscripcion)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].idJugador").value((int) jugadorClub));

        mockMvc.perform(post("/inscripciones/{id}/jugadores", idInscripcion)
                        .header("Authorization", "Bearer " + organizador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "idJugador", jugadorAjeno, "numeroCamiseta", 9, "posicion", "Delantero"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo podés convocar jugadores que pertenezcan a este club"));
    }

    @Test
    void bajaConWalkoverConservaResultadosJugadosYResuelveLosPendientes() throws Exception {
        String organizador = registrarOrganizador();
        String ajeno = registrarOrganizador();
        Map<String, Object> request = new LinkedHashMap<>(torneo());
        request.put("cantidadEquipos", 4);
        long torneoId = postJson("/torneos", organizador, request).get("idTorneo").asLong();
        long equipoBaja = postJson("/equipos", organizador, equipo("Equipo saliente", "Azul", torneoId))
                .get("idEquipo").asLong();
        for (int indice = 1; indice <= 3; indice++) {
            postJson("/equipos", organizador, equipo("Rival " + indice, "Rojo", torneoId));
        }
        for (int indice = 1; indice <= 2; indice++) {
            postJson("/jugadores", organizador,
                    jugador("Jugador" + indice, "Saliente", indice, equipoBaja));
        }

        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", organizador, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        java.util.List<JsonNode> partidosDelEquipo = new java.util.ArrayList<>();
        fixture.forEach(partido -> {
            if (partido.get("idEquipoLocal").asLong() == equipoBaja
                    || partido.get("idEquipoVisitante").asLong() == equipoBaja) {
                partidosDelEquipo.add(partido);
            }
        });
        org.junit.jupiter.api.Assertions.assertEquals(3, partidosDelEquipo.size());
        JsonNode jugado = partidosDelEquipo.get(0);
        putJson("/partidos/" + jugado.get("idPartido").asLong() + "/resultado", organizador,
                Map.of("golesLocal", 2, "golesVisitante", 1)).andExpect(status().isOk());

        mockMvc.perform(put("/inscripciones/equipos/torneo/{torneo}/equipo/{equipo}/baja",
                        torneoId, equipoBaja)
                        .header("Authorization", "Bearer " + ajeno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "resolucionPartidos", "WALKOVER", "motivo", "Baja administrativa"))))
                .andExpect(status().isForbidden());

        putJson("/inscripciones/equipos/torneo/" + torneoId + "/equipo/" + equipoBaja + "/baja",
                organizador, Map.of("resolucionPartidos", "WALKOVER", "motivo", "Baja administrativa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BAJA"))
                .andExpect(jsonPath("$.cantidadJugadores").value(0));

        mockMvc.perform(get("/partidos/{id}", jugado.get("idPartido").asLong())
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.golesLocal").value(2))
                .andExpect(jsonPath("$.golesVisitante").value(1));

        for (JsonNode pendiente : partidosDelEquipo.subList(1, partidosDelEquipo.size())) {
            boolean salienteEsLocal = pendiente.get("idEquipoLocal").asLong() == equipoBaja;
            mockMvc.perform(get("/partidos/{id}", pendiente.get("idPartido").asLong())
                            .header("Authorization", "Bearer " + organizador))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                    .andExpect(jsonPath("$.golesLocal").value(salienteEsLocal ? 0 : 3))
                    .andExpect(jsonPath("$.golesVisitante").value(salienteEsLocal ? 3 : 0))
                    .andExpect(jsonPath("$.observaciones").value(
                            org.hamcrest.Matchers.containsString("Baja de Equipo Saliente")));
        }

        String historial = mockMvc.perform(get("/inscripciones/equipos/{id}", equipoBaja)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("BAJA"))
                .andReturn().getResponse().getContentAsString();
        long inscripcionId = jsonMapper.readTree(historial).get(0).get("idInscripcion").asLong();
        mockMvc.perform(get("/inscripciones/{id}/jugadores", inscripcionId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[*].estado", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("INACTIVA"))));

        mockMvc.perform(get("/torneos/{id}/tabla", torneoId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idEquipo == " + equipoBaja + ")]").exists());
    }

    @Test
    void bajaConCancelacionNoOtorgaPuntosNiInventaUnResultado() throws Exception {
        String organizador = registrarOrganizador();
        long torneoId = postJson("/torneos", organizador, torneo()).get("idTorneo").asLong();
        long equipoBaja = postJson("/equipos", organizador, equipo("Equipo cancelado", "Azul", torneoId))
                .get("idEquipo").asLong();
        postJson("/equipos", organizador, equipo("Equipo restante", "Rojo", torneoId));
        JsonNode fixture = postJson("/partidos/torneo/" + torneoId + "/generar-fixture", organizador, Map.of(
                "fechaInicio", "2026-09-05", "horaInicio", "14:00", "diasEntreJornadas", 7,
                "minutosEntrePartidos", 60, "cancha", "Cancha principal"));
        long partidoId = fixture.get(0).get("idPartido").asLong();

        putJson("/inscripciones/equipos/torneo/" + torneoId + "/equipo/" + equipoBaja + "/baja",
                organizador, Map.of("resolucionPartidos", "CANCELAR", "motivo", "No continúa"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/partidos/{id}", partidoId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.golesLocal").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.golesVisitante").value(org.hamcrest.Matchers.nullValue()));
        mockMvc.perform(get("/torneos/{id}/tabla", torneoId)
                        .header("Authorization", "Bearer " + organizador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].puntos", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is(0))));
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
