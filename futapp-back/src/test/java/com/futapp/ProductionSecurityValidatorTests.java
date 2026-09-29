package com.futapp;

import com.futapp.config.ProductionSecurityValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionSecurityValidatorTests {
    @Test
    void aceptaUnaConfiguracionDeProduccionSegura() {
        var validator = new ProductionSecurityValidator(secureEnvironment());
        assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
    }

    @Test
    void rechazaClavesDeEjemploYOrigenesLocales() {
        var environment = secureEnvironment()
                .withProperty("spring.datasource.password", "futapp_dev")
                .withProperty("futapp.cors.allowed-origins", "http://localhost:5173");
        var validator = new ProductionSecurityValidator(environment);
        assertThrows(IllegalStateException.class,
                () -> validator.run(new DefaultApplicationArguments()));
    }

    @Test
    void rechazaTokensDeRecuperacionExpuestos() {
        var environment = secureEnvironment()
                .withProperty("futapp.password-reset.expose-token", "true");
        var validator = new ProductionSecurityValidator(environment);
        assertThrows(IllegalStateException.class,
                () -> validator.run(new DefaultApplicationArguments()));
    }

    private MockEnvironment secureEnvironment() {
        return new MockEnvironment()
                .withProperty("spring.datasource.password", "una-clave-larga-y-unica")
                .withProperty("futapp.cors.allowed-origins", "https://futapp.example.com")
                .withProperty("futapp.password-reset.expose-token", "false")
                .withProperty("futapp.demo-data.enabled", "false")
                .withProperty("springdoc.api-docs.enabled", "false")
                .withProperty("springdoc.swagger-ui.enabled", "false");
    }
}
