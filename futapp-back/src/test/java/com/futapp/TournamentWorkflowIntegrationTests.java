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
        postJson("/tarjetas", token, Map.of("minuto", 31, "tipoTarjeta", "AMARILLA", "idPartido", partidoId,
                "idJugador", jugadorVisitante.get("idJugador").asLong(), "idEquipo", visitante.get("idEquipo").asLong()));

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
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
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
                .andExpect(jsonPath("$.nombre").value("Liga integral"));
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
                .andExpect(jsonPath("$.divisiones[0].nombre").value("División única"))
                .andExpect(jsonPath("$.divisiones[0].fases", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.divisiones[0].fases[0].tipo").value("ZONAS"))
                .andExpect(jsonPath("$.divisiones[0].fases[0].zonas", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.divisiones[0].fases[1].tipo").value("ELIMINACION_DIRECTA"))
                .andExpect(jsonPath("$.divisiones[0].fases[1].clasificados").value(4))
                .andExpect(jsonPath("$.divisiones[0].cupos", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$.divisiones[0].cupos[0].etiqueta").value("Equipo 1"))
                .andExpect(jsonPath("$.divisiones[0].cupos[0].equipo").value("Equipo 1"))
                .andExpect(jsonPath("$.divisiones[0].cupos[7].etiqueta").value("Equipo 8"));
    }

    private String registrarOrganizador() throws Exception {
        String email = "workflow-" + System.nanoTime() + "@test.local";
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Organizador Workflow", email, "password123", "ORGANIZADOR"))))
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
