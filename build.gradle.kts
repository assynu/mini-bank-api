import org.flywaydb.core.Flyway
import org.testcontainers.postgresql.PostgreSQLContainer

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.testcontainers:testcontainers-postgresql:2.0.5")
        classpath("org.flywaydb:flyway-core:13.10.0")
        classpath("org.flywaydb:flyway-database-postgresql:13.10.0")
        classpath("org.postgresql:postgresql:42.7.14")
    }
}

plugins {
    groovy
    application
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
}

group = "com.bartczakdawid"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(platform("org.spockframework:spock-bom:2.4-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.flywaydb:flyway-core:13.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:13.10.0")
    implementation("org.slf4j:slf4j-api:2.0.20")
    implementation("org.jooq:jooq:3.21.9")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("tools.jackson.core:jackson-databind:3.2.3")

    runtimeOnly("org.postgresql:postgresql:42.7.14")
    runtimeOnly("ch.qos.logback:logback-classic:1.6.5")
}

application {
    mainClass = "com.bartczakdawid.minibank.App"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.bartczakdawid.minibank.jooq"
            }
        }
    }
}

val migrations = layout.projectDirectory.dir("src/main/resources/db/migration")

tasks.named("jooqCodegen") {
    inputs.dir(migrations)

    var postgres: PostgreSQLContainer? = null

    doFirst {
        val container = PostgreSQLContainer("postgres:18")
        container.start()
        postgres = container

        Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("filesystem:${migrations.asFile.absolutePath}")
            .load()
            .migrate()

        jooq {
            configuration {
                jdbc {
                    driver = "org.postgresql.Driver"
                    url = container.jdbcUrl
                    user = container.username
                    password = container.password
                }
            }
        }
    }

    doLast {
        postgres?.stop()
    }
}

tasks.compileJava {
    dependsOn("jooqCodegen")
}