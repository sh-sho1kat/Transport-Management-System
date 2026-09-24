Yes. From your current position, use **Codex to build `backend-next` incrementally while keeping the existing `backend` as the working reference implementation**. Do not ask Codex to “refactor the whole project according to this documentation” in one task.

Your final documentation is already structured for this approach: it defines a modular monolith, the final package structure, Flyway migration sequence, and explicitly requires vertical increments of **migration → entity/repository → business logic → API → tests → Git commit**.   

OpenAI's current Codex guidance also recommends keeping a long specification in repository files rather than repeatedly pasting it, then using a milestone plan with explicit validation criteria. `AGENTS.md` can provide persistent repository instructions to Codex. 

# 1. Your repository should become the Codex workspace

Keep roughly this structure:

```text
Transport-Management-System/
│
├── backend/                  # CURRENT system - reference only
│
├── backend-next/             # NEW implementation - Codex works here
│
├── frontend/                 # keep unchanged initially
│
├── docs/
│   ├── FINAL_SPEC.md
│   ├── CURRENT_STATE.md
│   ├── MIGRATION_MAP.md
│   ├── API_COMPATIBILITY.md
│   └── PLAN.md
│
├── AGENTS.md
│
└── README.md
```

Put the complete 5,000-line documentation you gave me into:

```text
docs/FINAL_SPEC.md
```

Do **not** paste those 5,000 lines into every Codex prompt.

Codex can read the file itself. OpenAI specifically recommends repository-local task/specification files for substantial coding-agent work. 

---

# 2. First protect your current working project

Before Codex modifies anything:

```bash
git status
git add .
git commit -m "chore: preserve current working system before v2 implementation"
git tag v1-baseline
```

Then:

```bash
git switch -c develop-v2
```

The important rule is:

```text
backend/
    READ ONLY

backend-next/
    NEW DEVELOPMENT
```

Codex may inspect `backend/` to understand existing functionality.

Codex should not refactor `backend/`.

---

# 3. Do not confuse backend V2 with API `/api/v2`

This distinction matters.

Your **implementation** is effectively:

```text
backend        = old implementation
backend-next   = new implementation
```

But your final documentation specifies:

```text
/api/v1
```

as the public API base. 

Therefore:

```text
backend-next ≠ /api/v2
```

It is perfectly valid for:

```text
backend-next
```

to implement:

```text
/api/v1/auth
/api/v1/stations
/api/v1/routes
/api/v1/trips
/api/v1/bookings
```

because this is the **first finalized public API contract**.

---

# 4. Create `AGENTS.md`

Put this at the repository root:

```md
# Bus Reservation & Fleet Management System — Codex Instructions

## Source of truth

The authoritative target architecture and behavior are defined in:

docs/FINAL_SPEC.md

Read only the sections relevant to the current task. Do not reinterpret or redesign documented business rules unless a contradiction makes implementation impossible.

## Repository boundaries

backend/ is the current working implementation.

Treat backend/ as read-only unless the task explicitly instructs otherwise.

backend-next/ is the new implementation.

All new backend development must occur in backend-next/.

frontend/ must remain unchanged until a task explicitly requests frontend integration.

## Architecture

Implement a modular monolith.

Use Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, Hibernate, PostgreSQL, Flyway, Maven, Docker, JUnit 5, Mockito and Testcontainers.

Follow feature/domain-based packaging defined in docs/FINAL_SPEC.md.

Do not convert the project into microservices.

## Database

Flyway is the schema authority.

Do not use Hibernate ddl-auto=update.

Use ddl-auto=validate outside disposable development scenarios.

Never edit an already-applied Flyway migration.

Do not create destructive migrations unless explicitly instructed.

Use BigDecimal for monetary values.

Use PostgreSQL NUMERIC(12,2) for monetary values.

Use TIMESTAMPTZ for timestamps.

## API

Base API path is /api/v1.

Never expose JPA entities directly.

Use request DTOs, response DTOs and mappers.

Use the documented centralized error format.

## Development process

Implement exactly one documented increment at a time.

For each increment:

1. Inspect existing relevant code.
2. Inspect relevant sections of docs/FINAL_SPEC.md.
3. Implement required Flyway migration.
4. Implement entities and repositories.
5. Implement business logic.
6. Implement DTOs, mappers and controllers.
7. Implement unit/integration tests.
8. Run relevant tests.
9. Fix failures before finishing.

Do not automatically begin the next increment.

## Quality rules

Do not create empty placeholder classes for future increments.

Do not add undocumented dependencies without justification.

Do not duplicate business logic across services.

Preserve transactional boundaries.

Do not trust monetary values supplied by the frontend.

Do not log passwords, JWTs, refresh tokens or payment secrets.

For concurrency-sensitive booking operations, follow the locking model defined in docs/FINAL_SPEC.md exactly.

## Completion report

After every task report:

- files changed
- migrations added
- APIs added or changed
- tests added
- commands executed
- test results
- unresolved risks or specification conflicts
```

Codex CLI automatically discovers `AGENTS.md` files and applies repository-specific instructions. 

---

# 5. Your first Codex task should NOT write code

Open the repository root in Codex.

If using Codex CLI:

```bash
cd Transport-Management-System
codex
```

Then give it this prompt:

```text
Study this repository before making any changes.

The current working backend is in backend/.
The replacement implementation is in backend-next/.
The frontend is in frontend/.
The authoritative final architecture and requirements are in docs/FINAL_SPEC.md.
Repository rules are in AGENTS.md.

For this task, DO NOT modify any source code.

Inspect:

1. backend/
2. backend-next/
3. frontend/
4. database/Flyway migrations if present
5. pom.xml files
6. Docker configuration
7. existing APIs
8. docs/FINAL_SPEC.md

Determine the exact current state of backend-next relative to the final specification.

Produce:

docs/CURRENT_STATE.md
docs/MIGRATION_MAP.md
docs/API_COMPATIBILITY.md
docs/PLAN.md

CURRENT_STATE.md must show what already exists, what is partially implemented, and what is missing.

MIGRATION_MAP.md must map current backend concepts/entities/tables to the target model.

API_COMPATIBILITY.md must map frontend API usage and current backend endpoints against the target /api/v1 endpoints.

PLAN.md must convert Increment 0 through the Version 1.0 scope in FINAL_SPEC.md into executable milestones.

Do not implement functionality yet.

Do not modify backend/.
Do not modify frontend/.
Do not invent requirements not present in FINAL_SPEC.md.

At the end, summarize the repository state and identify the first implementation milestone.
```

This is the first thing I would run.

---

# 6. Why the audit comes first

Your documentation describes the **desired final system**, not necessarily the exact difference between the current code and target code.

Codex needs to discover things such as:

```text
Already implemented?
Partially implemented?
Wrong architecture?
Reusable?
Needs replacement?
Database compatible?
Frontend dependent?
```

For example, your final specification requires:

```text
User
UserRole
RefreshToken
```

followed by JWT, BCrypt, refresh and logout. 

If `backend-next` already implements half of this correctly, Codex should complete it rather than generate a second authentication implementation.

---

# 7. Handle the database carefully

This is the most important preliminary decision.

Your final documentation proposes:

```text
V1__create_users.sql
...
V20__add_core_indexes.sql
```

and says migrations should be created incrementally. 

For `backend-next` development, use a **separate development database** initially.

Example:

```text
current backend
       ↓
tms_v1 database


backend-next
       ↓
tms_v2 database
```

Do not immediately point both applications at the same database.

For example:

```yaml
backend:
    DB_NAME: tms_v1

backend-next:
    DB_NAME: tms_v2
```

This allows Codex to construct the documented V1–V20 target schema cleanly.

If you eventually need existing V1 production data migrated into V2, that becomes a separate migration project involving:

```text
schema mapping
data conversion
backfill
verification
cutover
```

Do not mix that problem into normal feature implementation.

---

# 8. Then verify Increment 0

Your specification defines Increment 0 as foundation configuration: Maven, PostgreSQL, Docker Compose, Flyway, profiles, exception handling, auditing and Actuator, with the application starting successfully and `/actuator/health` returning `UP`. 

Give Codex:

```text
Implement/verify Increment 0 from docs/FINAL_SPEC.md.

Work only in backend-next/.

First inspect what Increment 0 functionality already exists. Preserve correct existing implementation rather than rewriting it unnecessarily.

Required target:

- Java 21
- Maven
- Spring Boot
- PostgreSQL
- Docker Compose
- Flyway
- application.yml
- application-dev.yml
- application-test.yml
- application-prod.yml
- global exception infrastructure
- JPA auditing foundation
- Spring Boot Actuator
- /actuator/health

Use Flyway as schema authority.
Do not implement business entities yet.
Do not implement authentication yet.
Do not modify backend/.
Do not modify frontend/.

Run the application and relevant tests.

Acceptance criteria:

1. Maven build succeeds.
2. PostgreSQL starts through Docker Compose.
3. Spring Boot connects to PostgreSQL.
4. Flyway initializes successfully.
5. /actuator/health reports UP.
6. ddl-auto is not update.
7. profiles are configured correctly.
8. no business feature from Increment 1+ is introduced.

Fix failures before finishing.

At completion report:
- existing pieces retained
- files changed
- configuration added
- commands executed
- test/build results
- remaining Increment 0 issues

Do not begin Increment 1.
```

This pattern is important:

> **Implement one increment. Stop. Test it. Commit it.**

---

# 9. Commit after Increment 0

After reviewing Codex's changes:

```bash
git add .
git commit -m "chore: establish backend-next project foundation"
```

Then:

```bash
git status
```

should be clean.

Your Git history becomes a recovery mechanism.

---

# 10. Increment 1: Authentication

The specification requires User, UserRole and RefreshToken followed by registration, login, BCrypt, JWT, refresh, logout and RBAC. 

Prompt:

```text
Implement Increment 1 — Authentication from docs/FINAL_SPEC.md.

Work only in backend-next/.

Read the relevant sections covering:

- User
- UserRole
- RefreshToken
- authentication APIs
- JWT security architecture
- authorization
- users/user_roles/refresh_tokens database schema
- Increment 1 requirements

Do not implement Station or later increments.

Implement vertically:

Flyway migrations
→ entities
→ repositories
→ security
→ services
→ DTOs
→ controllers
→ exception handling
→ tests

Required APIs:

POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout

Use BCrypt.

Implement JWT access tokens and refresh-token storage according to the specification.

Never expose passwordHash.

Required tests include:

successful registration
duplicate email rejection
incorrect password rejection
invalid JWT rejection
expired JWT rejection
role authorization

Use Testcontainers PostgreSQL where database behavior matters.

Run all backend-next tests.

Do not modify backend/.
Do not modify frontend/.
Do not begin Increment 2.

At completion report migrations, endpoints, security decisions, tests and results.
```

Then:

```bash
git add .
git commit -m "feat: implement authentication and RBAC"
```

---

# 11. Continue in this exact order

Do **not** let Codex skip ahead because later functionality looks related.

| Build order | Documentation increment | Main result |
|---:|---|---|
| 0 | Foundation | Bootable production-style skeleton |
| 1 | Authentication | User/JWT/RBAC |
| 2 | Station | Station management |
| 3 | Route Network | Route + ordered RouteStops |
| 4 | Seat Layout | SeatLayout + Seat |
| 5 | Bus | Fleet buses |
| 6 | Driver | Driver lifecycle |
| 7 | Trip | Trip + driver assignments |
| 8 | TripSeat | seat snapshots |
| 9 | TripFare | segment/seat-type fares |
| 10 | Trip Search | route/date search |
| 11 | Seat Availability | segment-aware availability |
| 12 | Basic Booking | Booking/Passenger/Allocation |
| 13 | Concurrency | pessimistic TripSeat locking |
| 14 | Multi-seat | atomic multi-seat reservation |
| 15 | Expiration | hold expiration |
| 16 | Mock Payment | gateway abstraction |
| 17 | Confirmation | booking/allocation confirmation |
| 18 | Idempotency | duplicate callback protection |
| 19 | Ticketing | passenger tickets + QR |
| 20 | Check-In | verification/check-in |
| 21 | Full Cancellation | booking cancellation |
| 22 | Partial Cancellation | passenger-level cancellation |
| 23 | Refund | refund workflow |
| Final V1 hardening | Testing/Swagger/Docker/CI | release-ready 1.0 |

That sequence comes directly from your specification.   

Do not implement Increment 24, real payment sandbox, until the mock payment flow is fully stable. Your documentation explicitly makes that dependency. 

---

# 12. Use one reusable Codex prompt after that

For Increment 2 onward, you do not need huge custom prompts.

Use this:

```text
Implement Increment <NUMBER> — <NAME> from docs/FINAL_SPEC.md.

Repository rules in AGENTS.md are mandatory.

Work only in backend-next/.

Before editing:
- inspect the relevant specification sections
- inspect existing code from completed increments
- inspect relevant Flyway migrations
- identify dependencies on previously completed increments

Implement this increment vertically:

database migration
→ entity/repository
→ service/business rules
→ DTO/mapper
→ controller/API
→ tests

Requirements:

- implement every business rule documented for this increment
- preserve previously completed behavior
- do not expose JPA entities
- use documented exception/error handling
- add appropriate database constraints/indexes
- add unit tests for business logic
- add PostgreSQL/Testcontainers tests when DB behavior matters
- add integration tests for the API
- run relevant tests plus the full backend-next test suite

Do not:
- modify backend/
- modify frontend/
- implement future increments
- create empty future classes
- weaken existing tests
- bypass failures
- replace documented behavior with a different design

If the specification conflicts with existing code, preserve the specification and report the conflict before making an incompatible architectural assumption.

At completion report:

1. files changed
2. migration added
3. endpoints added/changed
4. business rules implemented
5. tests added
6. commands executed
7. test results
8. remaining risks

Stop after Increment <NUMBER>.
```

Replace only:

```text
<NUMBER>
<NAME>
```

for each run.

---

# 13. For difficult increments, give Codex additional acceptance criteria

Booking is where you should become much stricter.

For Increment 13:

```text
Implement Increment 13 — Booking Concurrency.

Follow sections 33–37 of docs/FINAL_SPEC.md exactly.

Critical rule:
Lock TripSeat, not SeatAllocation.

Use PostgreSQL row-level pessimistic write locking.

For multi-seat operations, sort TripSeat IDs before obtaining locks.

The active allocation overlap rule is:

existingFrom < requestedTo
AND
existingTo > requestedFrom

HELD and CONFIRMED allocations block availability.

Required concurrency integration tests:

2 simultaneous requests:
same TripSeat
same segment

Expected:
1 succeeds
1 receives conflict

Then test 20 concurrent requests.

Expected:
1 succeeds
19 fail with seat conflict.

Tests must run against PostgreSQL Testcontainers, not H2.

Do not mock the repository in concurrency tests.

Do not proceed until concurrency tests reliably pass.
```

That directly implements one of the core architectural decisions in your documentation: lock `TripSeat` because an allocation row may not yet exist. 

---

# 14. Booking itself should be one atomic transaction

Your specification defines the exact transaction:

```text
Authenticate
↓
Trip validation
↓
RouteStop validation
↓
sort TripSeat IDs
↓
lock TripSeats
↓
check overlapping allocations
↓
calculate fare
↓
Booking
↓
Passengers
↓
HELD allocations
↓
expiresAt
↓
COMMIT
```



Tell Codex not to distribute this across several unrelated transactions.

Use:

```java
@Transactional
```

around the orchestration boundary.

---

# 15. Make Codex prove each increment works

Never accept:

```text
Implemented successfully.
```

as verification.

Require actual commands.

Typical Spring Boot verification:

```bash
./mvnw clean test
```

and later:

```bash
./mvnw verify
```

For Windows:

```powershell
mvnw.cmd clean test
```

For database-backed testing:

```bash
docker compose up -d postgres
```

and then:

```bash
./mvnw test
```

For foundation:

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

The exact port can differ depending on your V1/V2 configuration.

---

# 16. Use different ports while both backends exist

A useful local configuration is:

```text
Existing backend:
localhost:8080

backend-next:
localhost:8081
```

Likewise:

```text
V1 DB:
localhost:5432 / tms_v1

V2 DB:
localhost:5433 / tms_v2
```

This prevents accidental collisions.

Your old frontend can continue talking to:

```text
localhost:8080
```

while you develop V2 on:

```text
localhost:8081
```

---

# 17. Do not touch the frontend yet

Your final passenger flow depends on many backend increments:

```text
Login
→ Search
→ Availability
→ Booking
→ Hold
→ Payment
→ Confirmation
→ Ticket
```



If Codex modifies the frontend while those APIs are still moving, you will repeatedly rewrite both sides.

Instead use:

```text
frontend → current backend
```

during early V2 development.

Only after:

```text
Auth
Stations
Routes
Fleet
Trips
Search
Availability
Booking
Payment
Tickets
```

are stable should you begin frontend cutover.

---

# 18. But make Codex build an API compatibility matrix now

Your first audit should produce something such as:

```text
Frontend call             Current backend         Final backend
-----------------------------------------------------------------------
POST /login               /api/auth/login         /api/v1/auth/login
GET /routes               /routes                 /api/v1/routes
GET /trip/search          ...                     /api/v1/trips/search
POST /booking             ...                     /api/v1/bookings
```

Then, when backend V2 is finished, you know exactly what the frontend requires.

Do not discover those differences at the end.

---

# 19. After core V1, run a separate Codex hardening task

Do not immediately deploy.

Prompt:

```text
Perform Version 1.0 release hardening for backend-next.

Use docs/FINAL_SPEC.md Version 1.0 scope as the authoritative checklist.

Do not add Version 1.1 or Version 1.2 features.

Audit every required feature and mark it:

IMPLEMENTED
PARTIAL
MISSING
BROKEN

Then fix all Version 1.0 deficiencies.

Verify:

- authentication
- RBAC
- station management
- routes and RouteStops
- seat layouts/seats
- buses
- drivers
- trips
- driver assignments
- TripFares
- TripSeats
- trip search
- seat availability
- booking
- temporary holds
- pessimistic locking
- concurrency
- multi-seat atomicity
- payment attempts
- idempotency
- tickets
- QR verification
- check-in
- cancellation
- partial cancellation
- refunds
- Swagger/OpenAPI
- PostgreSQL/Flyway
- JUnit
- Testcontainers
- Docker
- GitHub Actions

Run the complete automated test suite.

Run Maven verify.

Inspect Flyway migrations from an empty PostgreSQL database.

Run a full end-to-end integration flow:

register
→ login
→ search
→ availability
→ booking
→ payment
→ confirmation
→ ticket
→ check-in

Also test:

payment failure
booking expiration
duplicate payment callback
concurrent booking
full cancellation
partial cancellation
refund

Do not modify frontend yet.

Produce docs/V1_RELEASE_AUDIT.md containing evidence for every Version 1.0 requirement.
```

Your specification's Version 1.0 checklist is explicit. 

---

# 20. Then let Codex integrate the existing frontend

Only now give:

```text
Integrate the existing frontend with backend-next.

First read docs/API_COMPATIBILITY.md.

The existing frontend behavior and UI should remain unchanged unless an API contract requires a minimal adaptation.

backend-next is now the authoritative backend.

Do not redesign the frontend.

Map frontend calls to the documented /api/v1 API.

Handle JWT access tokens and refresh behavior correctly.

Verify these complete user flows:

register/login
trip search
trip selection
seat availability
seat selection
booking
payment
booking confirmation
ticket viewing
cancellation

Verify relevant admin flows as well.

Run frontend tests/build and backend integration tests.

Report every frontend file changed and every API contract adaptation.
```

This fulfills your original goal:

```text
new Spring Boot backend
+
same frontend
```

without building both simultaneously.

---

# 21. Then delete `backend-next` as a concept

Once V2 passes all tests:

```text
backend/        ← old
backend-next/   ← final new backend
```

Rename:

```text
backend/
    ↓
backend-legacy/
```

and:

```text
backend-next/
    ↓
backend/
```

Do this in its own commit:

```bash
git commit -m "refactor: promote backend-next as primary backend"
```

Do not combine that rename with feature changes.

---

# 22. Your Git history should resemble this

```text
chore: preserve current working system before v2 implementation

chore: establish backend-next project foundation

feat: implement authentication and RBAC

feat: implement station management

feat: implement ordered route network

feat: implement seat layout management

feat: implement bus management

feat: implement driver management

feat: implement trip scheduling

feat: generate trip seat snapshots

feat: implement trip fares

feat: implement trip search

feat: implement segment-aware seat availability

feat: implement booking and temporary seat holds

fix: prevent concurrent double booking

feat: implement atomic multi-seat reservation

feat: implement booking expiration

feat: implement mock payment gateway

feat: implement booking payment confirmation

feat: implement payment idempotency

feat: implement passenger ticket generation

feat: implement ticket verification and check-in

feat: implement booking cancellation

feat: implement partial passenger cancellation

feat: implement refunds

test: complete version 1 integration suite

ci: add production build pipeline

refactor: promote backend-next as primary backend
```

Your documentation already proposes essentially this Git strategy. 

---

# 23. Do not use multiple Codex agents initially

Current Codex supports parallel/multi-agent workflows. 

Do **not** initially do:

```text
Agent A → Booking
Agent B → Payment
Agent C → TripSeat
Agent D → TripFare
```

simultaneously.

Those modules depend heavily on one another.

You risk incompatible:

```text
entities
DTOs
migrations
method signatures
transaction boundaries
```

Build the core chain sequentially:

```text
Route
↓
Trip
↓
TripSeat
↓
Fare
↓
Availability
↓
Booking
↓
Payment
↓
Ticket
```

Parallel agents become useful later for relatively independent work such as:

```text
test coverage
documentation
OpenAPI cleanup
Docker hardening
CI
static analysis
```

after interfaces are stable.

---

# 24. Your Codex workflow should therefore be

```text
FINAL_SPEC.md
      │
      ▼
Repository audit
      │
      ▼
PLAN.md
      │
      ▼
Increment 0
      │
   tests pass
      │
    commit
      ▼
Increment 1
      │
   tests pass
      │
    commit
      ▼
Increment 2
      │
     ...
      ▼
Increment 23
      │
      ▼
V1 release audit
      │
      ▼
Full integration tests
      │
      ▼
Frontend integration
      │
      ▼
Docker/CI
      │
      ▼
Staging
      │
      ▼
Production
```

That is the correct way to use Codex for this project: **documentation defines the architecture; Codex handles one bounded engineering milestone; automated tests prove the milestone; Git freezes the verified state; only then does the next milestone begin.**