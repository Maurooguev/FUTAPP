package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "futapp.features.player-accounts=false",
        "futapp.features.team-director-accounts=false",
        "futapp.features.claims=false",
        "futapp.features.notifications=false",
        "futapp.features.favorites=false"
})
@AutoConfigureMockMvc
class MvpFeatureFlagIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void registraOrganizadoresPeroNoCuentasDeJugadoresODirectores() throws Exception {
        registrar("ORGANIZADOR").andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ORGANIZADOR"));
        registrar("JUGADOR").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "En esta versión las cuentas son exclusivamente para organizadores de torneos"));
        registrar("DIRECTOR_EQUIPO").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "En esta versión las cuentas son exclusivamente para organizadores de torneos"));
    }

    @Test
    void bloqueaFuncionesAvanzadasSinCerrarLosPerfilesPublicos() throws Exception {
        String response = registrar("ORGANIZADOR").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = jsonMapper.readTree(response).get("token").asString();

        mockMvc.perform(get("/reclamos/pendientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/notificaciones").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/favoritos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/equipos/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/jugadores/999999")).andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions registrar(String rol) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                        "Cuenta " + rol, rol.toLowerCase() + "-" + System.nanoTime() + "@test.local",
                        "password123", rol))));
    }
}
