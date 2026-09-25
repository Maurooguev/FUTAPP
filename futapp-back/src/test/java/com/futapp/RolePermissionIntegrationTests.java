package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import com.futapp.model.Usuario;
import com.futapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RolePermissionIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;
    @Autowired UsuarioRepository usuarios;

    @Test
    void visitanteSoloAccedeALaInformacionPublica() throws Exception {
        mockMvc.perform(get("/explorar")).andExpect(status().isOk());
        mockMvc.perform(get("/favoritos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/torneos/mios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/equipos/mios")).andExpect(status().isUnauthorized());
    }

    @Test
    void jugadorNoAdministraTorneosEquiposPerfilesAjenosNiArchivos() throws Exception {
        String token = registrar("JUGADOR");

        mockMvc.perform(get("/torneos/mios").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/equipos/mios").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/equipos/creados").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfiles-organizador/mio").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", bearer(token))).andExpect(status().isNotFound());
        mockMvc.perform(multipart("/archivos/imagenes")
                        .file(imagen()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void directorSoloAccedeALaAdministracionDeEquipos() throws Exception {
        String token = registrar("DIRECTOR_EQUIPO");

        mockMvc.perform(get("/equipos/mios").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/torneos/mios").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/equipos/creados").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfiles-organizador/mio").header("Authorization", bearer(token))).andExpect(status().isForbidden());
    }

    @Test
    void organizadorSoloAccedeALaAdministracionDeTorneos() throws Exception {
        String token = registrar("ORGANIZADOR");

        mockMvc.perform(get("/torneos/mios").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/equipos/creados").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/equipos/mios").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfil-jugador/mio").header("Authorization", bearer(token))).andExpect(status().isForbidden());
        mockMvc.perform(get("/perfiles-organizador/mio").header("Authorization", bearer(token))).andExpect(status().isNotFound());
    }

    @Test
    void administradorConservaAccesoDeSupervision() throws Exception {
        String email = "permission-admin-" + System.nanoTime() + "@test.local";
        String token = registrar("ORGANIZADOR", email);
        Usuario admin = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        admin.setRol("ADMIN");
        usuarios.saveAndFlush(admin);

        mockMvc.perform(get("/torneos/mios").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/equipos/mios").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/auditoria").header("Authorization", bearer(token))).andExpect(status().isOk());
    }

    private String registrar(String rol) throws Exception {
        return registrar(rol, "permission-" + rol.toLowerCase() + "-" + System.nanoTime() + "@test.local");
    }

    private String registrar(String rol, String email) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new RegisterRequestDTO("Cuenta " + rol, email, "password123", rol))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("token").asString();
    }

    private MockMultipartFile imagen() {
        return new MockMultipartFile("archivo", "foto.png", "image/png", new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});
    }

    private String bearer(String token) { return "Bearer " + token; }
}
