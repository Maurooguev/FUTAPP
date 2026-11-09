package com.futapp;

import com.futapp.model.AuthToken;
import com.futapp.model.Usuario;
import com.futapp.repository.AuthTokenRepository;
import com.futapp.repository.UsuarioRepository;
import com.futapp.security.TokenSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "futapp.registration.enabled=false",
        "futapp.features.player-accounts=false",
        "futapp.features.team-director-accounts=false"
})
@AutoConfigureMockMvc
class InvitationAccessIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository users;
    @Autowired AuthTokenRepository tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired JsonMapper json;

    @BeforeEach void clean() { tokens.deleteAll(); users.deleteAll(); }

    @Test void blocksPublicRegistrationAndNonOrganizerLogin() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Organizador\",\"email\":\"org@test.local\",\"password\":\"password123\",\"rol\":\"ORGANIZADOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Las cuentas de organizador se habilitan únicamente por invitación"));

        Usuario player = user("Jugador", "jugador@test.local", "JUGADOR", "password123", false);
        users.save(player);
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"jugador@test.local\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El acceso de esta versión es exclusivo para organizadores de torneos"));
    }

    @Test void adminCreatesTemporaryAccessAndOrganizerMustChangePassword() throws Exception {
        Usuario admin = users.save(user("Admin", "admin@test.local", "ADMIN", "admin-password", false));
        String adminToken = tokenFor(admin);
        String invitationBody = mvc.perform(post("/admin/organizers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Liga Federal\",\"email\":\"federal@test.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temporaryPassword").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String temporaryPassword = json.readTree(invitationBody).get("temporaryPassword").asText();

        String loginBody = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"federal@test.local\",\"password\":\"" + temporaryPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordChangeRequired").value(true))
                .andReturn().getResponse().getContentAsString();
        String organizerToken = json.readTree(loginBody).get("token").asText();

        mvc.perform(get("/torneos/mios/panel").header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Debés cambiar la contraseña temporal antes de continuar"));
        mvc.perform(put("/auth/password").header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"" + temporaryPassword + "\",\"nuevaPassword\":\"definitiva-123\"}"))
                .andExpect(status().isOk());
    }

    private Usuario user(String name, String email, String role, String password, boolean temporary) {
        Usuario user = new Usuario(); user.setNombre(name); user.setEmail(email); user.setRol(role);
        user.setPasswordHash(encoder.encode(password)); user.setPasswordChangeRequired(temporary); return user;
    }

    private String tokenFor(Usuario user) {
        String raw = TokenSecurity.generate(); AuthToken token = new AuthToken();
        token.setToken(TokenSecurity.hash(raw)); token.setUsuario(user); token.setExpiresAt(LocalDateTime.now().plusHours(1));
        tokens.save(token); return raw;
    }
}
