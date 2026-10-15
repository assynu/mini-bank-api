package com.bartczakdawid.minibank

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

abstract class IntegrationSpec extends Specification {
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18")
    static final DSLContext dsl

    static {
        postgres.start()

        def config = new HikariConfig()
        config.jdbcUrl = postgres.jdbcUrl
        config.username = postgres.username
        config.password = postgres.password
        def dataSource = new HikariDataSource(config)

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate()

        dsl = DSL.using(dataSource, SQLDialect.POSTGRES)
    }

    def setup() {
        dsl.execute("TRUNCATE accounts, transfers, account_history")
    }
}