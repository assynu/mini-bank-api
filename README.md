# Mini Bank API ![CI](https://github.com/assynu/mini-bank-api/actions/workflows/ci.yml/badge.svg)

A banking backend built to explore what makes payment systems hard: money
precision, concurrent transfers, idempotent requests and a double-entry ledger.
Simplified by design: no real money, no real customers.

## Requirements

- JDK 21
- Docker, running. The build uses it to generate jOOQ classes from the
  migrations, and the integration tests use it for a throwaway PostgreSQL.

Running the application only needs a PostgreSQL database; Docker is required
for building and testing.

## Running

### API and database in Docker

Builds the application on your machine, then starts PostgreSQL and the API in containers.

```bash
./gradlew installDist
docker compose --profile app up --build
```

The image is built from `build/install`, not inside Docker, because the build
itself needs Docker to generate jOOQ classes.

### API on your machine, database in Docker

```bash
docker compose up -d
./gradlew run
```

`docker compose up` without a profile starts only PostgreSQL. To use your own
PostgreSQL instead, skip it and set the variables from
[Configuration](#configuration).

## Testing

```bash
./gradlew test
```

Integration and API tests start their own PostgreSQL in a container
(Testcontainers), so they don't touch the database from `compose.yaml`.

`http/accounts.http` contains example requests for the IntelliJ HTTP Client,
with assertions for every endpoint.

## Configuration

Set these environment variables to override the defaults.

| Variable            | Default                                     |
|---------------------|---------------------------------------------|
| `DATABASE_URL`      | `jdbc:postgresql://localhost:5432/minibank` |
| `DATABASE_USERNAME` | `minibank`                                  |
| `DATABASE_PASSWORD` | `minibank`                                  |
| `PORT`              | `8080`                                      |