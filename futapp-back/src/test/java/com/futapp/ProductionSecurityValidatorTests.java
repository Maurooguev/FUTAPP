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

    @Test
    void rechazaSecretosDeLaPlantillaSinReemplazar() {
        var environment = secureEnvironment()
                .withProperty("spring.datasource.password", "REEMPLAZAR_CON_CLAVE_SEGURA")
                .withProperty("futapp.registration.invite-code", "REEMPLAZAR_CON_CODIGO_PRIVADO")
                .withProperty("spring.mail.password", "REEMPLAZAR_CON_CLAVE_SMTP");
        var validator = new ProductionSecurityValidator(environment);
        assertThrows(IllegalStateException.class,
                () -> validator.run(new DefaultApplicationArguments()));
    }

    private MockEnvironment secureEnvironment() {
        return new MockEnvironment()
                .withProperty("spring.datasource.password", "una-clave-larga-y-unica")
                .withProperty("futapp.cors.allowed-origins", "https://beta.futapp.ar")
                .withProperty("futapp.password-reset.expose-token", "false")
                .withProperty("futapp.password-reset.mail-enabled", "true")
                .withProperty("futapp.registration.invite-code", "beta-invitacion-segura")
                .withProperty("futapp.public-url", "https://beta.futapp.ar")
                .withProperty("futapp.mail.from", "no-reply@futapp.ar")
                .withProperty("spring.mail.host", "smtp.proveedor.test")
                .withProperty("spring.mail.username", "no-reply@futapp.ar")
                .withProperty("spring.mail.password", "clave-smtp-segura")
                .withProperty("futapp.demo-data.enabled", "false")
                .withProperty("springdoc.api-docs.enabled", "false")
                .withProperty("springdoc.swagger-ui.enabled", "false");
    }
}
