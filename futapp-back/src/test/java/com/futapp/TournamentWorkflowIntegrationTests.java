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

        postJson("/jugadores", token, jugador("Juan", "Norte", 10, local.get("idEquipo").asLong()));
        postJson("/jugadores", token, jugador("Pedro", "Sur", 9, visitante.get("idEquipo").asLong()));

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

        putJson("/partidos/" + partidoId + "/resultado", token,
                Map.of("golesLocal", 3, "golesVisitante", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.golesLocal").value(3))
                .andExpect(jsonPath("$.golesVisitante").value(1));

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

        long torneoId = postJson("/torneos", token, torneo()).get("idTorneo").asLong();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ORGANIZADOR"));

        mockMvc.perform(get("/torneos/mios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idTorneo").value(torneoId));

        putJson("/torneos/" + torneoId, token, torneo())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Liga integral"));
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
        return Map.of(
                "nombre", "Liga integral",
                "descripcion", "Torneo creado por la prueba del circuito principal",
                "ubicacion", "Buenos Aires",
                "estado", "BORRADOR",
                "formato", "LIGA",
                "cantidadEquipos", 2
        );
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
