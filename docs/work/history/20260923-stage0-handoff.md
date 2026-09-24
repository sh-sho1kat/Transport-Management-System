# Verified handoff — 2026-09-23

## Completed changes

Stage 0 only: independent backend-next Spring Boot application, Java 21 compiler target, copied Maven wrapper, dev/test/prod profiles, separate PostgreSQL helper, Flyway foundation migration, JPA validation, health endpoint, safe JSON errors and default-denied stateless security. No login or business endpoints exist yet.

Added the target package/MVC ownership map, preserved full source specification, database relationships, target role scope, current API inventory, implemented replacement OpenAPI, migration decisions/mapping/cutover guides, ordered roadmap and short root AGENTS.md. Added a separate replacement CI workflow and read-only migration inventory helper. Optional Docker location contains documentation only.

Current backend, frontend and existing local-db.py are untouched: SHA-256 comparison passed for all 163 protected files. Original Flyway V1–V3 unchanged. Pre-existing uncommitted work was preserved. The two existing files edited for navigation/ignores (README.md and .gitignore) were backed up under `.local/code-backups/next-foundation-20260923-174241`.

## Tests run and results

- Initial Maven `clean verify`: passed (4 tests); final Maven `verify` after error tests/formatting: passed (6 tests, 0 failures/errors/skips).
- Local validation used `/tmp/apache-maven-3.9.11/bin/mvn -o -Dmaven.repo.local=/tmp/tms-m2` with installed Java 25 and release 21 compiler target. The standard reproducible developer command is `./mvnw clean verify`; wrapper download behavior and CI Java 21 runtime were not exercised here.
- Foundation tests use real PostgreSQL 16 in wayline_next_test: migration marker, healthy connection, denied business/write/actuator-detail requests and no session cookie.
- Error tests: malformed JSON returns 400; unexpected errors return safe 500 without private details.
- Architecture test: package ownership, MVC controller isolation, shared isolation, module API boundaries, no business-to-workflow dependencies.
- `python3 scripts/checks/next-contract.py`: passed (foundation contract shape; not a full OpenAPI semantic validator).
- `python3 scripts/migration/inventory.py`: passed, read-only; three legacy migrations, one new migration, 41 legacy API paths.
- Packaged dev application on port 8089: GET /actuator/health returned 200/UP; GET /api/v2/bookings returned 401/UNAUTHORIZED. The dev migration applied independently; repeat test startup validated the existing test migration successfully.
- Original specification copy verified byte-for-byte. No old-backend/frontend tests were rerun because their protected files did not change.

## Known failures or unfinished work

No failures in executed checks. Hosted CI has not run. Local Java tooling emits existing deprecation/Mockito agent warnings. Source-based architecture checks are guardrails, not proof of transaction correctness or complete dependency analysis. Stage 1–16 and later extensions are unimplemented; no production readiness, JWT, accounts, CORS integration, importer, payments or inventory are claimed.

## Migration/configuration notes

Replacement API port 8089; private PostgreSQL port 55433; database/user wayline_next; local password NextDb123!; separate test DB wayline_next_test. The normal local helper on 55432 is unchanged. Stop the temporary verification app and replacement cluster after verification; start `python3 scripts/next-db.py start` before the next IntelliJ run. Data is preserved.

Stage 0 V1 is a foundation marker; next business migration is V2. The source specification's example numbering and /api/v1 routes are superseded by ADR 0001. Keep the existing frontend on backend port 8088. No application login credentials exist for backend-next.

## Exact next task

Follow current-task.md: Stage 1A accounts and role persistence only, with specification-confirmed fields, V2 users/roles, BCrypt, explicit permission vocabulary and database/policy tests. Keep routes closed. JWT, refresh lifecycle and public registration/login belong to a later reviewed slice. Do not implement the entire roadmap in one session.
