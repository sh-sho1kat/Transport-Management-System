# Replacement backend — Stage 0

A separately runnable Spring Boot foundation. Business features, JWT login and tickets are not implemented yet. Keep using `backend/` and the existing frontend for working operations.

## Local run (no Docker)

Use Java 21 or newer with Java 21 compiler compatibility. PostgreSQL server tools and Python 3 must be installed. Maven wrapper downloads Maven/dependencies on the first run if not cached.

From the **project root**, in a terminal:

```bash
python3 scripts/next-db.py start
```

Open `backend-next/pom.xml` as a Maven project in IntelliJ. Select JDK 21, then run `com.tms.TmsApplication` using the **backend-next module classpath**. Default profile is `dev`; no environment variables or `.env` plugin are needed. The old backend has the same main class name, so check the selected module.

In Postman: `GET http://localhost:8089/actuator/health` returns `200 {"status":"UP"}` when PostgreSQL is reachable. Other routes return a JSON 401 until authentication is implemented. No application usernames or passwords exist yet.

| DBeaver field | Value |
|---|---|
| Host | 127.0.0.1 |
| Port | 55433 |
| Database | wayline_next |
| Username | wayline_next |
| Password | NextDb123! |
| Test database | wayline_next_test (same credentials) |

These are local development credentials only. The helper creates a separate cluster at `.local/postgres-next`; it never uses the existing cluster on 55432. The dev role owns its private local cluster; production needs separately provisioned restricted roles.

## Verify

Start the helper above, then from `backend-next/`:

```bash
./mvnw clean verify
```

Tests use `wayline_next_test`, never the dev database. Architecture tests enforce package boundaries. Foundation tests check real PostgreSQL migrations, health, denied requests and absence of session cookies. To use another dedicated test database set `NEXT_TEST_DB_URL`, `NEXT_TEST_DB_USER`, `NEXT_TEST_DB_PASSWORD`.

From the project root:

```bash
python3 scripts/checks/next-contract.py
python3 scripts/migration/inventory.py
python3 scripts/next-db.py status
python3 scripts/next-db.py stop
```

`stop` preserves data. Restart the helper before running the backend after a reboot. Keep running the current frontend with `npm run dev` in `frontend/`; it continues using port 8088 and `/api/v1`.

## Configuration

- `application.yml`: common safe defaults, port `NEXT_SERVER_PORT` (8089 by default), Flyway, JPA validation, health.
- `application-dev.yml`: loopback-only local database defaults.
- `application-test.yml`: dedicated PostgreSQL test database.
- `application-prod.yml`: requires `NEXT_DB_URL`, `NEXT_DB_USER`, `NEXT_DB_PASSWORD`; select explicitly with `SPRING_PROFILES_ACTIVE=prod`.

The production profile is a configuration placeholder, not release readiness. CORS denies cross-origin API use at Stage 0. Stage 1 must define JWT/refresh transport, CSRF policy and explicit frontend origin allowlists before browser integration. Do not expose the dev profile publicly.

See [roadmap](../docs/work/roadmap.md) and [handoff](../docs/work/handoff.md).
