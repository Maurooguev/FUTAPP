package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "futapp.registration.invite-code=beta-secreta-2026")
@AutoConfigureMockMvc
class PrivateBetaRegistrationIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;

    @Test
    void exigeElCodigoDeInvitacionConfigurado() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Invitado", "sin-codigo@test.local", "password123", "JUGADOR", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El código de invitación no es válido"));
    }

    @Test
    void permiteRegistrarAlInvitadoConElCodigoCorrecto() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Invitado", "con-codigo@test.local", "password123", "JUGADOR", "beta-secreta-2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
