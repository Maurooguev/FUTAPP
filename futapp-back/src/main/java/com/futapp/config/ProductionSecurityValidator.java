package com.futapp.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Profile("prod")
public class ProductionSecurityValidator implements ApplicationRunner {
    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "futapp_dev", "futapp", "password", "postgres", "changeme", "admin", "12345678");
    private final Environment environment;

    public ProductionSecurityValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configuración de producción insegura: " + String.join("; ", errors));
        }
    }

    List<String> validate() {
        List<String> errors = new ArrayList<>();
        String password = environment.getProperty("spring.datasource.password", "").trim();
        if (password.length() < 12 || WEAK_PASSWORDS.contains(password.toLowerCase(Locale.ROOT))) {
            errors.add("DATABASE_PASSWORD debe tener al menos 12 caracteres y no puede ser una clave de ejemplo");
        }

        String origins = environment.getProperty("futapp.cors.allowed-origins", "");
        if (origins.isBlank()) {
            errors.add("CORS_ALLOWED_ORIGINS es obligatorio");
        } else {
            for (String origin : origins.split("\\s*,\\s*")) {
                String normalized = origin.toLowerCase(Locale.ROOT);
                if (!normalized.startsWith("https://") || normalized.contains("localhost")
                        || normalized.contains("127.0.0.1") || normalized.contains("*")) {
                    errors.add("cada origen CORS de producción debe ser HTTPS, explícito y no local");
                    break;
                }
            }
        }

        if (environment.getProperty("futapp.password-reset.expose-token", Boolean.class, false)) {
            errors.add("PASSWORD_RESET_EXPOSE_TOKEN debe ser false");
        }
        if (environment.getProperty("futapp.demo-data.enabled", Boolean.class, false)) {
            errors.add("DEMO_DATA_ENABLED debe ser false");
        }
        if (environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false)
                || environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, false)) {
            errors.add("OPENAPI_ENABLED debe permanecer false en producción");
        }
        return errors;
    }
}
