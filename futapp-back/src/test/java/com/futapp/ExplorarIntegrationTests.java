package com.futapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExplorarIntegrationTests {
    @Autowired MockMvc mockMvc;

    @Test
    void explorarEsPublicoYDevuelveLaEstructuraCompleta() throws Exception {
        mockMvc.perform(get("/explorar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.torneos").isArray())
                .andExpect(jsonPath("$.ultimosResultados").isArray())
                .andExpect(jsonPath("$.proximosPartidos").isArray())
                .andExpect(jsonPath("$.ubicaciones").isArray());
    }
}
