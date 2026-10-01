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
        if (password.length() < 12 || isPlaceholder(password) || WEAK_PASSWORDS.contains(password.toLowerCase(Locale.ROOT))) {
            errors.add("DATABASE_PASSWORD debe tener al menos 12 caracteres y no puede ser una clave de ejemplo");
        }

        String origins = environment.getProperty("futapp.cors.allowed-origins", "");
        if (origins.isBlank()) {
            errors.add("CORS_ALLOWED_ORIGINS es obligatorio");
        } else {
            for (String origin : origins.split("\\s*,\\s*")) {
                String normalized = origin.toLowerCase(Locale.ROOT);
                if (!normalized.startsWith("https://") || normalized.contains("localhost")
                        || normalized.contains("127.0.0.1") || normalized.contains("*") || isPlaceholder(normalized)) {
                    errors.add("cada origen CORS de producción debe ser HTTPS, explícito y no local");
                    break;
                }
            }
        }

        if (environment.getProperty("futapp.password-reset.expose-token", Boolean.class, false)) {
            errors.add("PASSWORD_RESET_EXPOSE_TOKEN debe ser false");
        }
        if (!environment.getProperty("futapp.password-reset.mail-enabled", Boolean.class, false)) {
            errors.add("PASSWORD_RESET_MAIL_ENABLED debe ser true");
        }
        String inviteCode = environment.getProperty("futapp.registration.invite-code", "");
        if (inviteCode.length() < 12 || isPlaceholder(inviteCode)) {
            errors.add("BETA_INVITE_CODE debe tener al menos 12 caracteres");
        }
        String publicUrl = environment.getProperty("futapp.public-url", "").trim().toLowerCase(Locale.ROOT);
        if (!publicUrl.startsWith("https://") || publicUrl.contains("localhost")
                || publicUrl.contains("127.0.0.1") || isPlaceholder(publicUrl)) {
            errors.add("PUBLIC_URL debe ser una dirección HTTPS pública");
        }
        if (environment.getProperty("spring.mail.host", "").isBlank()
                || isPlaceholder(environment.getProperty("spring.mail.host", ""))
                || environment.getProperty("spring.mail.username", "").isBlank()
                || isPlaceholder(environment.getProperty("spring.mail.username", ""))
                || environment.getProperty("spring.mail.password", "").length() < 8
                || isPlaceholder(environment.getProperty("spring.mail.password", ""))
                || environment.getProperty("futapp.mail.from", "").isBlank()
                || isPlaceholder(environment.getProperty("futapp.mail.from", ""))) {
            errors.add("la configuración SMTP debe incluir host, usuario, clave y remitente");
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

    private boolean isPlaceholder(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("reemplazar") || normalized.contains("example");
    }
}
