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
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    void permiteElFrontendConfiguradoMedianteCors() throws Exception {
        mockMvc.perform(options("/status")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
