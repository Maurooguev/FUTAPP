package com.futapp;

import com.futapp.dto.RegisterRequestDTO;
import com.futapp.model.Usuario;
import com.futapp.repository.UsuarioRepository;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.security.TokenSecurity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RolePermissionIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired AuthTokenRepository authTokens;

    @Test
    void visitanteSoloAccedeALaInformacionPublica() throws Exception {
        mockMvc.perform(get("/explorar")).andExpect(status().isOk());
        mockMvc.perform(get("/favoritos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/torneos/mios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/equipos/mios")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/torneos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/equipos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/jugadores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/partidos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/partidos/1/historial-resultado")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/auditoria")).andExpect(status().isUnauthorized());
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
        mockMvc.perform(post("/torneos").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/partidos/1/historial-resultado").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/auditoria").header("Authorization", bearer(token)))
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
        mockMvc.perform(post("/torneos").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/equipos/999999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/equipos/999998/fusionar/999999").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/partidos/1/historial-resultado").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
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

    @Test
    void laSesionFuncionaConCookieHttpOnlyYSeRevocaAlCerrarSesion() throws Exception {
        String email = "cookie-session-" + System.nanoTime() + "@test.local";
        var register = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new RegisterRequestDTO("Cuenta Cookie", email, "password123", "JUGADOR"))))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Cookie session = register.getCookie("FUTAPP_SESSION");
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertTrue(session.isHttpOnly());

        mockMvc.perform(get("/auth/me").cookie(session)).andExpect(status().isOk());
        mockMvc.perform(post("/auth/logout").cookie(session)).andExpect(status().isOk());
        mockMvc.perform(get("/auth/me").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void unaSesionVencidaNoAutenticaYSeElimina() throws Exception {
        String token = registrar("JUGADOR");
        String hash = TokenSecurity.hash(token);
        var session = authTokens.findById(hash).orElseThrow();
        session.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        authTokens.saveAndFlush(session);

        mockMvc.perform(get("/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
        org.junit.jupiter.api.Assertions.assertFalse(authTokens.existsById(hash));
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
