package com.futapp;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
class PostgresMigrationIT {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("futapp.demo-data.enabled", () -> "false");
    }

    @Autowired DataSource dataSource;

    @Test
    void appliesEveryMigrationAndUsesTimestampForMatches() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT data_type FROM information_schema.columns
                     WHERE table_name = 'partido' AND column_name = 'fecha_hora'
                     """)) {
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo("timestamp without time zone");
            }
        }

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT character_maximum_length FROM information_schema.columns
                     WHERE table_name = 'torneo' AND column_name = 'descripcion'
                     """)) {
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isEqualTo(500);
            }
        }
    }
}
