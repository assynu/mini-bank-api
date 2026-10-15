package com.bartczakdawid.minibank;

import com.bartczakdawid.minibank.repository.AccountHistoryRepository;
import com.bartczakdawid.minibank.repository.AccountRepository;
import com.bartczakdawid.minibank.repository.Database;
import com.bartczakdawid.minibank.repository.JooqTransactions;
import com.bartczakdawid.minibank.service.AccountService;
import com.bartczakdawid.minibank.service.Transactions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        DataSource dataSource = createDataSource();
        migrateDatabase(dataSource);

        DSLContext dsl = Database.createDslContext(dataSource);

        AccountRepository accountRepository = new AccountRepository(dsl);
        AccountHistoryRepository accountHistoryRepository = new AccountHistoryRepository(dsl);
        Transactions transactions = new JooqTransactions(dsl);

        AccountService accountService = new AccountService(accountRepository, accountHistoryRepository, transactions);

        log.info("Mini Bank API started");
    }

    private static DataSource createDataSource() {
        var config = new HikariConfig();

        config.setJdbcUrl(getEnv("DATABASE_URL", "jdbc:postgresql://localhost:5432/minibank"));
        config.setUsername(getEnv("DATABASE_USERNAME", "minibank"));
        config.setPassword(getEnv("DATABASE_PASSWORD", "minibank"));

        return new HikariDataSource(config);
    }

    private static void migrateDatabase(DataSource dataSource) {
        var migrationResult = Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        String version = migrationResult.targetSchemaVersion != null
                ? migrationResult.targetSchemaVersion
                : migrationResult.initialSchemaVersion;

        log.atInfo()
                .addKeyValue("applied", migrationResult.migrationsExecuted)
                .addKeyValue("version", version)
                .log("Database migrated");
    }

    private static String getEnv(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}