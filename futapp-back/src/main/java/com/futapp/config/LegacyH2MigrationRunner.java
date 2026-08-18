package com.futapp.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "futapp.legacy-h2-migration.enabled", havingValue = "true")
public class LegacyH2MigrationRunner implements ApplicationRunner {
    private static final List<String> TABLES = List.of(
            "usuario", "torneo", "equipo", "jugador", "fecha",
            "partido", "gol", "tarjeta", "auth_token"
    );
    private static final List<String> IDENTITIES = List.of(
            "usuario:id_usuario", "torneo:id_torneo", "equipo:id_equipo",
            "jugador:id_jugador", "fecha:id_fecha", "partido:id_partido",
            "gol:id_gol", "tarjeta:id_tarjeta"
    );

    private final DataSource targetDataSource;
    private final Environment environment;
    private final ConfigurableApplicationContext applicationContext;

    public LegacyH2MigrationRunner(DataSource targetDataSource, Environment environment,
                                   ConfigurableApplicationContext applicationContext) {
        this.targetDataSource = targetDataSource;
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String sourceUrl = environment.getRequiredProperty("futapp.legacy-h2-migration.url");
        String sourceUser = environment.getProperty("futapp.legacy-h2-migration.username", "sa");
        String sourcePassword = environment.getProperty("futapp.legacy-h2-migration.password", "");

        try (Connection source = DriverManager.getConnection(sourceUrl, sourceUser, sourcePassword);
             Connection target = targetDataSource.getConnection()) {
            target.setAutoCommit(false);
            ensureEmptyTarget(target);
            try {
                for (String table : TABLES) {
                    int copied = copyTable(source, target, table);
                    System.out.printf("Migración H2 → PostgreSQL: %s (%d filas)%n", table, copied);
                }
                resetIdentities(target);
                target.commit();
                System.out.println("Migración H2 → PostgreSQL completada correctamente.");
            } catch (Exception migrationError) {
                target.rollback();
                throw migrationError;
            }
        }

        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    private void ensureEmptyTarget(Connection target) throws Exception {
        for (String table : TABLES) {
            try (Statement statement = target.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                result.next();
                if (result.getLong(1) > 0) {
                    throw new IllegalStateException("PostgreSQL ya contiene datos en " + table
                            + ". La migración solo se ejecuta sobre una base vacía.");
                }
            }
        }
    }

    private int copyTable(Connection source, Connection target, String table) throws Exception {
        try (Statement sourceStatement = source.createStatement();
             ResultSet rows = sourceStatement.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData metadata = rows.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                columns.add(metadata.getColumnLabel(index).toLowerCase(Locale.ROOT));
            }
            String placeholders = String.join(", ", columns.stream().map(column -> "?").toList());
            String sql = "INSERT INTO " + table + " (" + String.join(", ", columns)
                    + ") VALUES (" + placeholders + ")";
            int copied = 0;
            try (PreparedStatement insert = target.prepareStatement(sql)) {
                while (rows.next()) {
                    for (int index = 1; index <= columns.size(); index++) {
                        insert.setObject(index, rows.getObject(index));
                    }
                    insert.addBatch();
                    copied++;
                }
                if (copied > 0) insert.executeBatch();
            }
            return copied;
        }
    }

    private void resetIdentities(Connection target) throws Exception {
        try (Statement statement = target.createStatement()) {
            for (String identity : IDENTITIES) {
                String[] parts = identity.split(":");
                statement.execute("SELECT setval(pg_get_serial_sequence('" + parts[0] + "', '" + parts[1]
                        + "'), COALESCE(MAX(" + parts[1] + "), 1), MAX(" + parts[1] + ") IS NOT NULL) FROM " + parts[0]);
            }
        }
    }
}
