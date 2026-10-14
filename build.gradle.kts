plugins {
    groovy
    application
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
    testImplementation(platform("org.spockframework:spock-bom:2.4-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.flywaydb:flyway-core:12.8.0")
    implementation("org.flywaydb:flyway-database-postgresql:12.8.0")

    runtimeOnly("org.postgresql:postgresql:42.7.14")
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