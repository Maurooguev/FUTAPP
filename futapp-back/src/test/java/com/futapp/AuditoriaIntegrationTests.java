package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import com.futapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;
    @Autowired UsuarioRepository usuarios;

    @Test
    void registraOperacionesYRestringeElHistorialAAdministradores() throws Exception {
        String email = "audit-" + System.nanoTime() + "@test.local";
        String registro = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO(
                                "Auditor", email, "password123", "ORGANIZADOR"))))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn().getResponse().getContentAsString();
        String token = jsonMapper.readTree(registro).get("token").asString();

        mockMvc.perform(post("/torneos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "nombre", "Torneo auditado", "descripcion", "Prueba", "ubicacion", "Merlo",
                                "estado", "BORRADOR", "formato", "Liga", "cantidadEquipos", 2))))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));

        mockMvc.perform(get("/auditoria").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        var admin = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        admin.agregarRol("ADMIN");
        usuarios.saveAndFlush(admin);

        mockMvc.perform(get("/auditoria").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].ruta", hasItem("/torneos")))
                .andExpect(jsonPath("$.content[*].metodo", hasItem("POST")))
                .andExpect(jsonPath("$.content[*].requestId").isNotEmpty());
    }
}
