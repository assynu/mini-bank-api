package com.bartczakdawid.minibank;

import org.flywaydb.core.Flyway;

public class App {
    public static void main(String[] args) {
        databaseMigration();

        System.out.println("Mini Bank API started");
    }

    private static void databaseMigration() {
        String url = System.getenv().getOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/minibank");
        String username = System.getenv().getOrDefault("DATABASE_USERNAME", "minibank");
        String password = System.getenv().getOrDefault("DATABASE_PASSWORD", "minibank");

        Flyway.configure()
                .dataSource(url, username, password)
                .load()
                .migrate();

        System.out.println("Database migrated successfully");
    }
}
