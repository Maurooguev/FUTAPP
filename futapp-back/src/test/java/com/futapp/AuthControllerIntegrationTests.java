package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.security.TokenSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private AuthTokenRepository authTokens;

    @Test
    void devuelveLaSesionActualYLaInvalidaAlCerrarSesion() throws Exception {
        String registration = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Organizador API", "api@test.local", "password123", "ORGANIZADOR"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = jsonMapper.readTree(registration).get("token").asString();

        org.junit.jupiter.api.Assertions.assertFalse(authTokens.existsById(token));
        org.junit.jupiter.api.Assertions.assertTrue(authTokens.existsById(TokenSecurity.hash(token)));

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("api@test.local"))
                .andExpect(jsonPath("$.rol").value("ORGANIZADOR"));

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
    }

    @Test
    void protegeLaSesionSinTokenConUnErrorJson() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
                .andExpect(header().string("Cache-Control", "no-store, max-age=0"));
    }

    @Test
    void permiteElFrontendConfiguradoMedianteCors() throws Exception {
        mockMvc.perform(options("/status")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    void rechazaUnaModificacionIniciadaDesdeOtroSitio() throws Exception {
        mockMvc.perform(put("/favoritos/torneos/1")
                        .header("Origin", "https://sitio-malicioso.example"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Origen de solicitud no permitido"));
    }

    @Test
    void marcaComoPrivadaUnaRespuestaAutenticadaPorCookie() throws Exception {
        var registration = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Cookie segura", "cookie@test.local", "password123", "JUGADOR"))))
                .andExpect(status().isOk()).andReturn().getResponse();
        String token = jsonMapper.readTree(registration.getContentAsString()).get("token").asString();

        mockMvc.perform(get("/auth/me").cookie(new Cookie("FUTAPP_SESSION", token)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store, max-age=0"));
    }

    @Test
    void conservaSoloLasCincoSesionesMasRecientes() throws Exception {
        String email = "sesiones@test.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Sesiones", email, "password123", "JUGADOR"))))
                .andExpect(status().isOk());

        String oldestLoginToken = null;
        String newestLoginToken = null;
        for (int index = 0; index < 6; index++) {
            String response = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            String token = jsonMapper.readTree(response).get("token").asString();
            if (index == 0) oldestLoginToken = token;
            newestLoginToken = token;
        }

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + oldestLoginToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + newestLoginToken))
                .andExpect(status().isOk());
    }

    @Test
    void permiteCerrarTodasLasSesionesDeLaCuenta() throws Exception {
        String registration = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Cerrar sesiones", "cerrar-sesiones@test.local", "password123", "JUGADOR"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String firstToken = jsonMapper.readTree(registration).get("token").asString();
        String login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cerrar-sesiones@test.local\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String secondToken = jsonMapper.readTree(login).get("token").asString();

        mockMvc.perform(post("/auth/logout-all").header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isUnauthorized());
    }
}
