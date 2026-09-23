# Architecture

## Runtime and migration layout

One Spring Boot application and one PostgreSQL database is the eventual deployment. During migration, `backend/` remains the running service and `backend-next/` is an isolated replacement. It is not a second production service. The frontend remains unchanged until a reviewed cutover.

```text
backend/                  existing application, port 8088
frontend/                 existing frontend, port 5178
backend-next/             replacement, port 8089
  pom.xml, mvnw, mvnw.cmd, .mvn/
  src/main/java/com/tms/
    TmsApplication.java
    config/               security wiring now; properties/observability when needed
    shared/
      exception/          centralized safe error handling
      response/           error DTO
  src/main/resources/
    application.yml
    application-dev.yml
    application-test.yml
    application-prod.yml
    db/migration/
  src/test/java/com/tms/
    architecture/
    FoundationTest.java
docs/                     specification, architecture, contracts and handoff
scripts/
  local-db.py             unchanged existing database helper
  next-db.py              separate replacement dev/test database helper
  checks/
  migration/
infra/docker/             documentation only; no Docker prerequisite
.github/workflows/        existing CI plus isolated replacement checks
```

## Target ownership map

Create each feature only when its milestone starts. Empty package trees and placeholder business implementations are intentionally absent.

| Top-level package | Feature packages |
|---|---|
| `identity` | auth, user, token, security |
| `network` | station, route |
| `fleet` | layout, bus, driver (maintenance later) |
| `trip` | scheduling, inventory, fare, search (template later) |
| `reservation` | booking, allocation, availability, expiration, cancellation |
| `finance` | payment, refund, gateway/mock (gateway/provider later) |
| `ticketing` | ticket, checkin |
| `workflow` | checkout, cancellation |
| `reporting` | read projections |
| `audit` | persistent business audit (later) |
| `notification` | delivery after commit (later) |

`config` wires application components. `shared` contains only feature-independent exceptions, response types, persistence and validation helpers when needed.

## MVC within each feature

```text
network/station/
  controller/StationController.java
  dto/request/CreateStationRequest.java
  dto/request/UpdateStationRequest.java
  dto/response/StationResponse.java
  service/StationService.java
  entity/Station.java
  repository/StationRepository.java
  mapper/StationMapper.java
```

Controllers validate transport input, call services and return DTOs. Services own business use cases and transaction boundaries. Repositories perform persistence; entities never become API responses. Mappers translate representations without making business decisions.

Use concrete services. Add interfaces for genuine seams such as PaymentGateway or a module's public API. Add `policy`, `domain`, `api`, `job`, `event` only when they contain real code.

## Dependency rules

- Controllers cannot reference repositories or entities.
- Each top-level module owns writes to its tables. Other modules depend on explicitly exposed `api` packages, never another module's repositories/entities/internal services.
- Public APIs expose immutable DTOs or IDs, not managed entities or repositories. Database foreign keys still enforce relationships across module boundaries.
- `workflow` coordinates use cases spanning modules. Business modules must never call workflow. Dependency direction must remain acyclic.
- `shared` may depend only on other shared types and libraries. `config` may wire modules but holds no business logic.
- Reporting uses read projections; notifications execute after committed changes.
- Payment confirmation atomically updates payment, booking, allocations and tickets. Gateway network calls stay outside that transaction.
- Allocation locks sorted TripSeat IDs. Confirmation and expiry follow a documented consistent booking-then-seat lock order. Concurrency tests ship with the first writable booking feature.

`ArchitectureTest` currently checks source references, root ownership, shared isolation, cross-module API access and controller boundaries. It is a lightweight guard, not proof of runtime transaction safety; expand it and concurrency tests with each feature. No reflection-based boundary bypasses.

## Adding a feature in a short session

Read current-task.md and only relevant specification sections. Implement one vertical slice: migration, entity/repository, DTO/mapper, service/policy, controller, contract and tests. Run checks. Update handoff with exact commands/results and the next bounded slice. Do not add future-stage endpoints returning fake success.
