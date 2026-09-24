> Replacement structure: [documentation index](docs/README.md), [Increment 0 run guide](backend-next/README.md), and [implementation roadmap](docs/work/roadmap.md). The current application below remains unchanged.

> Architecture refactor: [feature layout](ARCHITECTURE.md), [extension guide](docs/EXTENDING-THE-SYSTEM.md), and [production prerequisites](docs/PRODUCTION.md).

> Updated: counter staff, walk-in tickets, cash collection/refunds, and fare updates are documented in [Counter operations](docs/COUNTER-OPERATIONS.md).

# Wayline — Local Development Edition

General bus reservation and fleet management with **React + Spring Boot + PostgreSQL**.

**Start with [START-HERE.md](START-HERE.md).** It contains the exact IntelliJ Run-button setup, npm commands, DBeaver connection fields, all local usernames/passwords, and Postman instructions.

| Component | Address |
|---|---|
| Frontend | http://localhost:5178 |
| Backend | http://localhost:8088 |
| PostgreSQL | 127.0.0.1:55432 / wayline |

```bash
# Project root: start this project's isolated PostgreSQL instance
python3 scripts/local-db.py start
# Then open backend/pom.xml in IntelliJ and run TmsApplication.main().
# In another terminal:
cd frontend
npm ci
npm run dev
```

Optional demo data, from project root after backend startup:

```bash
python3 scripts/seed-demo.py
python3 scripts/check-connectivity.py
```

Docker configuration is removed. Local configuration is classpath-based; `.env` is no longer loaded. Open `backend/src/main/resources/application-local.properties` to see the defaults. No IDE-specific plugin is required to run the Java main class with a JDK and imported Maven dependencies.

## What is implemented

- Passenger registration, session login/logout, profile updates, password recovery with expiring single-use tokens, and CSRF protection.
- Administrator-created staff accounts, explicit roles, staff deactivation, session revocation, and audit records.
- Buses with unique registrations and editable seat layouts; active/archived stops and ordered routes.
- Draft → published → departed → completed trips, plus explicit trip cancellation.
- PostgreSQL foreign keys, unique inventory per trip/seat, and exclusion constraints preventing overlapping bus/driver assignments, including turnaround.
- Atomic multi-seat holds, server-calculated totals, repeat-safe confirmation, booking history, cancellation cutoffs, and administrator overrides.
- Assigned-driver manifests, CSV exports, occupancy and booking-value reports, printable tickets, and responsive role-aware screens.
- Flyway schema migrations, 19 PostgreSQL integration tests, API contract checking, container configuration, and fictional demo seeding.

Staff can record full cash collection at the counter or boarding, payment corrections, and completed cash refunds. Booked value is not collected revenue. Ticket printing uses the browser's Print/Save as PDF function; automated ticket email/PDF delivery and payment processing are outside this release.

## Documentation and API testing

- [Run guide and credentials](START-HERE.md)
- [Importable Postman collection](postman/Wayline-Local.postman_collection.json)
- [Database relationships](docs/DATABASE.md)
- [Architecture and Node-to-Spring mapping](ARCHITECTURE.md)
- [OpenAPI contract](docs/openapi.json) and [API guide](docs/API.md)
- [Operations](docs/OPERATIONS.md), [legacy migration](MIGRATION.md), [verification](VALIDATION.md)

## Build and integration tests

```bash
cd backend
./mvnw -DskipTests package
cd ../frontend
npm run build
```

For tests, create a separate `wayline_test` database. Never point the test suite at application data; it truncates tables.

```bash
PGPASSWORD='WaylineDb123!' createdb -h 127.0.0.1 -p 55432 -U wayline wayline_test
cd backend
TEST_DATABASE_URL=jdbc:postgresql://127.0.0.1:55432/wayline_test \
TEST_DATABASE_USER=wayline TEST_DATABASE_PASSWORD='WaylineDb123!' ./mvnw verify
```

The ZIP includes a prebuilt backend JAR and frontend assets under `release/`. IntelliJ and npm use the source directories; those binaries are optional. Start a Java 21+ JAR with `java -jar release/wayline-backend-2.0.0.jar` after starting PostgreSQL. Do not open frontend assets as file URLs; use npm or an HTTP server with an API proxy.

The earlier source is archived outside this release. There is no live legacy MongoDB endpoint. This is a single-operator development application; real payments, GPS, automated booking email and historical account claiming remain outside scope.
