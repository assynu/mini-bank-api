# Mini Bank API ![CI](https://github.com/assynu/mini-bank-api/actions/workflows/ci.yml/badge.svg)

A small banking backend I built to learn what makes payment systems hard:
getting money right, concurrent transfers, retried requests and an account
history that always adds up. No real money, no real customers, no frontend.

## What it does

- Open accounts in one currency, deposit money, transfer between accounts
- Every balance change is saved in the account's history, and the balance
  always equals the sum of its entries
- Concurrent transfers on the same account don't lose or create money
- Transfers in both directions at the same time don't deadlock
- Sending the same transfer twice (same `requestId`) moves money once

## API

| Method | Path                          | What it does                  |
|--------|-------------------------------|-------------------------------|
| POST   | `/accounts`                   | open an account               |
| GET    | `/accounts/{id}`              | account with its balance      |
| POST   | `/accounts/{id}/deposits`     | deposit money                 |
| GET    | `/accounts/{id}/transactions` | history, newest first         |
| POST   | `/transfers`                  | transfer between two accounts |
| GET    | `/transfers/{id}`             | a single transfer             |

Errors always look like `{"error": "INSUFFICIENT_FUNDS", "message": "..."}`:

| Status | When                                                                 |
|--------|----------------------------------------------------------------------|
| 400    | bad JSON or invalid values (`MALFORMED_REQUEST`, `INVALID_REQUEST`)  |
| 404    | `ACCOUNT_NOT_FOUND`, `TRANSFER_NOT_FOUND`, `NOT_FOUND`               |
| 409    | `requestId` reused for a different transfer (`IDEMPOTENCY_CONFLICT`) |
| 422    | business rule broken: `INSUFFICIENT_FUNDS`, `CURRENCY_MISMATCH`, `SAME_ACCOUNT_TRANSFER`, `INVALID_AMOUNT` |

Amounts are strings in JSON (`"30.00"`), so nothing gets rounded on the way.

## Why it's built this way

**Money.** `BigDecimal` plus `Currency`, never `double`. The scale comes from
the currency (2 for PLN, 0 for JPY), and anything more precise is rejected
instead of rounded. If a client sends `10.001 PLN`, that's probably a mistake anyway.

**Account history.** Each account keeps its balance, and every change also goes
into `account_history`: one entry for a deposit, two for a transfer (`-30` and
`+30`), so the balance always equals the sum of the entries.

**Concurrency.** A transfer reads both accounts with `SELECT ... FOR UPDATE`,
which locks the rows until the transaction ends. Without it, two requests can
read the same balance and the second write overwrites the first.

**Deadlocks.** Locks are always taken in the same order (smaller account id
first), whichever direction the transfer goes. Without that, A -> B and B -> A at
the same time can each hold one account and wait for the other forever.

**Idempotency.** `POST /transfers` needs a `requestId`. Same id and same data
returns the original transfer; same id with different data is a 409. The
column has a unique constraint, so even two identical requests at the exact
same moment end up as one transfer: the loser rolls back and returns the
winner's result.

**Stack.** Plain Java 21 with SparkJava, jOOQ, Flyway, HikariCP and Jackson.

**Tests.** Spock, and everything that touches the database runs against real
PostgreSQL in Testcontainers. The concurrency tests start threads
behind a latch so they can really overlap.

**Docker.** The image is built from `build/install` on the host, not inside
Docker, because generating jOOQ classes already needs Docker during the
build. CI builds the image, starts it with the database and sends a request,
so a broken image fails the pipeline.

## Requirements

- JDK 21
- Docker, **running**
> The build uses Docker to generate jOOQ classes from the migrations, and the integration tests use it for a throwaway PostgreSQL.

Running the application only needs a PostgreSQL database; Docker is required
for building and testing.

## Running

### API and database in Docker

Builds the application locally, then starts PostgreSQL and the API in containers.

```bash
./gradlew installDist
docker compose --profile app up --build
```

### API locally, database in Docker

```bash
docker compose up -d
./gradlew run
```

`docker compose up` without a profile starts only PostgreSQL. To use your own
PostgreSQL instead, skip it and set the variables from
[Configuration](#configuration).

### Try it

```bash
curl -s -X POST localhost:8080/accounts -H 'Content-Type: application/json' \
  -d '{"owner":"Anna","currency":"PLN"}'
curl -s -X POST localhost:8080/accounts -H 'Content-Type: application/json' \
  -d '{"owner":"Jan","currency":"PLN"}'

# replace <anna> and <jan> with the ids from above
curl -s -X POST localhost:8080/accounts/<anna>/deposits -H 'Content-Type: application/json' \
  -d '{"amount":"100.00","currency":"PLN"}'
curl -s -X POST localhost:8080/transfers -H 'Content-Type: application/json' \
  -d '{"requestId":"demo-1","fromAccountId":"<anna>","toAccountId":"<jan>","amount":"30.00","currency":"PLN"}'

curl -s localhost:8080/accounts/<anna>/transactions
```

Run the transfer again with the same `requestId`: you get the same transfer
back, and Anna still has 70.00.

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

## Not included

No authentication, currency exchange, withdrawals or history paging. I kept
the scope small on purpose and focused on getting transfers and balances right.