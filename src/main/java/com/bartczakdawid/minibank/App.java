package com.bartczakdawid.minibank;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        databaseMigration();

        log.info("Mini Bank API started");
    }

    private static void databaseMigration() {
        String url = System.getenv().getOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/minibank");
        String username = System.getenv().getOrDefault("DATABASE_USERNAME", "minibank");
        String password = System.getenv().getOrDefault("DATABASE_PASSWORD", "minibank");

        var migrationResult = Flyway.configure()
                .dataSource(url, username, password)
                .load()
                .migrate();

        String version = migrationResult.targetSchemaVersion != null
                ? migrationResult.targetSchemaVersion
                : migrationResult.initialSchemaVersion;

        log.atInfo()
                .addKeyValue("applied", migrationResult.migrationsExecuted)
                .addKeyValue("version", version)
                .log("Database migrated");    }
}
