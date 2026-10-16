# Mini Bank Api ![CI](https://github.com/assynu/mini-bank-api/actions/workflows/ci.yml/badge.svg)

A banking backend built to explore what makes payment systems hard: money
precision, concurrent transfers, idempotent requests and a double-entry ledger.
Simplified by design: no real money, no real customers.

## Requirements

- JDK 21
- Docker, running. The build uses it to generate jOOQ classes from the
  migrations, and the integration tests use it for a throwaway PostgreSQL.

Running the application only needs a PostgreSQL database; Docker is required
for building and testing.