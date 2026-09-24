# Current state — foundation alignment

Scope: replacement backend only. Existing application capabilities are reference behavior, not replacement implementation. Verified execution results belong in [handoff](work/handoff.md).

## Replacement requirements

| Requirement | Status | Evidence / boundary |
|---|---|---|
| Java 21, Maven, Spring Boot | IMPLEMENTED | backend-next/pom.xml, Maven wrapper; Java 21 verification recorded in handoff |
| PostgreSQL local setup | IMPLEMENTED | scripts/next-db.py, separate dev/test databases on 55433 |
| Profiles, environment configuration | IMPLEMENTED | backend-next/src/main/resources/application*.yml; production requires NEXT_DB_* |
| Flyway, ddl-auto validate | IMPLEMENTED | application.yml and db/migration/V1__foundation.sql; no schema changes in alignment |
| Feature foundation packages | IMPLEMENTED | backend-next/src/main/java/com/tms/common; future feature packages intentionally absent |
| Standard errors and request tracing | IMPLEMENTED | common/exception, common/response, common/config/RequestTraceFilter; six-field responses |
| Timestamp auditing | IMPLEMENTED | common/audit, UTC Clock; testfixture/audit/AuditProbe in test sources only |
| Actor attribution / business audit logs | DEFERRED | Identity not implemented; business AuditLog belongs to later extension |
| Health and default-denied security | IMPLEMENTED | common/security/SecurityConfiguration; health is only public endpoint |
| JWT, refresh, registration, roles | MISSING | Increment 1; no accounts or credentials in replacement |
| Stations, routes/stops, fleet/drivers | MISSING | Increments 2–6 |
| Trips, assignments, fares, snapshots, search | MISSING | Increments 7–10 |
| Availability, booking, passengers, allocation | MISSING | Increments 11–15; no writable inventory in replacement |
| Payments, confirmation, idempotency | MISSING | Increments 16–18 |
| Tickets, check-in, cancellation/refunds | MISSING | Increments 19–23 |
| OpenAPI / Swagger | PARTIAL | docs/api/next.yaml describes foundation only; Swagger UI and feature contracts are future |
| Architecture checks / CI | IMPLEMENTED / hosted run unverified | ArchitectureTest plus .github/workflows/backend-next.yml; local evidence in handoff |
| Docker and Testcontainers | DEFERRED | Explicit local-first exception in ADR 0002; real PostgreSQL tests used now |
| Frontend blueprint | DEFERRED | references/FRONTEND_BLUEPRINT.md retained; frontend is unchanged JavaScript React |
| Import / archive / cutover tooling | PARTIAL | Design documents and read-only inventory exist; no importer or cutover executed |
| Full Increment 0 from original text | PARTIAL by original checklist | Local foundation completed; Docker requirement explicitly deferred, not silently counted as done |

## Current working application (preserved)

backend/ is Spring Boot, not Node.js at this point. Its features are identity, catalog, scheduling, booking, reporting, audit and shared. It uses UUIDs, session cookies/CSRF, integer minor-unit money, whole-trip inventory, separate holds and cash status tracking. Current roles include DRIVER, which the target model intentionally does not retain as a login role.

frontend/package.json contains React/Vite JavaScript with a fetch client, not TypeScript/TanStack Query/Axios. frontend/src/shared/api/client.js prefixes /api/v1 and manages session/CSRF. Existing screen behavior remains on port 8088 through the current Vite proxy.

The repository was clean at inspection (baseline commit a27f008). There is no need to create a baseline commit/tag automatically. This task adds no Git commit.

## Reuse decisions

Keep the replacement's wrapper, pinned dependencies, profiles, health behavior, database helper and applied migration. Reorganize only foundation packages and expand errors/auditing/tests. Reuse existing application concepts as references, not by copying incompatible entities/contracts wholesale. See MIGRATION_MAP and API_COMPATIBILITY.
