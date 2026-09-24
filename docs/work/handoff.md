# Verified handoff — final-document alignment, 2026-09-23

## Completed changes

Increment 0 local foundation aligned with the newly approved documents. Preserved all four attachments verbatim; FINAL_SPEC.md is canonical and specification.md is a pointer. Added CURRENT_STATE, MIGRATION_MAP, API_COMPATIBILITY and PLAN, updated AGENTS/setup/index, and superseded earlier package/API choices through ADR 0002. The earlier handoff remains unchanged in [history](history/20260923-stage0-handoff.md).

Moved foundation code into common/config, common/security, common/exception and common/response. Added UTC Clock, timestamp-only JPA auditing and request tracing. All application/security errors use timestamp/status/code/message/path/traceId; timestamps are ISO text; X-Trace-Id matches the body and MDC is cleared. Health remains the only public endpoint. Target business prefix is /api/v1, but no business endpoint or authentication exists yet.

Added architecture cycle/boundary checks with negative examples, auditing persistence tests using a uniquely named temporary test schema, expanded error/security tests, version-neutral next.yaml contract, documentation checker and updated CI references.

## Commands and verified results

From project root:

```bash
python3 scripts/next-db.py start
python3 scripts/checks/next-contract.py
python3 scripts/checks/docs-links.py
python3 scripts/migration/inventory.py
git diff --check
```

Contract check passed. Documentation links checked (19 active documents; final link count printed by checker); immutable references/history excluded. Inventory: three original migrations, one replacement foundation migration, 41 legacy API paths. Protected-file SHA-256 check: all 165 backend/frontend/helper/applied-migration files unchanged. All four repository reference documents match the supplied attachments byte-for-byte.

Java 21 runtime was installed without javac 21, causing the first Maven attempt to fail compilation. Downloaded the matching Ubuntu openjdk-21-jdk-headless package to /tmp and combined it with the installed runtime in an isolated temporary toolchain, without installing system packages. A subsequent test caught numeric serialization of the error timestamp in standalone MVC; JsonFormat now enforces the specified ISO string.

Final successful build, from backend-next:

```bash
JAVA_HOME=/tmp/tms-align/jdk/usr/lib/jvm/java-21-openjdk-amd64 /tmp/apache-maven-3.9.11/bin/mvn -o -Dmaven.repo.local=/tmp/tms-m2 verify
```

**12 tests passed, 0 failures, 0 errors, 0 skipped**, on Java 21.0.12.1 and PostgreSQL 16.15. Tests: four foundation/security, four error/tracing, three architecture (including cycle/violation negative cases), one persistence auditing test. Initial clean build followed by successful verification after the timestamp fix. Final log: /tmp/tms-align-build-final.log. Standard developer command remains `./mvnw clean verify` with a complete JDK and started test database.

Packaged application started and stopped twice with Java 21 on 8089: GET /actuator/health returned 200/UP both times; GET /api/v1/bookings returned 401 with exactly six error fields, ISO timestamp and matching UUID trace header. Flyway history/checksum unchanged across both startups. No audit fixture schemas remained in the test database. Runtime logs: /tmp/tms-align-runtime-1.log and -2.log; result /tmp/tms-align-runtime-results.json. The local runtime verification script lives only in /tmp and is not required to run the project.

## Known limitations / deferred work

No failing application checks remain. Hosted GitHub Actions and Maven-wrapper downloads were not exercised. The host still needs a complete permanent JDK selected for IntelliJ/Maven; the temporary verification toolchain is not a permanent SDK setup. Existing Mockito dynamic-agent warnings remain.

Docker/Testcontainers are explicitly deferred, so original Increment 0 is not marked fully complete without that documented exception. Actor attribution, persistent audit logs, users/JWT, business tables, feature APIs, Swagger UI, importer and frontend integration are unimplemented. Source-level architecture checks are guardrails, not proof of transactional correctness.

An automatic permission-review attempt for runtime verification timed out; the subsequently approved prepared check completed. No work remains blocked by that timeout.

## Configuration and preservation

Current backend/frontend and both DB helpers are unchanged. Applied migrations are unchanged. Local replacement: 8089; PostgreSQL 55433; database/user wayline_next; local password NextDb123!; test database wayline_next_test. Future migrations start V2. NEXT_DB_URL/NEXT_DB_USER/NEXT_DB_PASSWORD remain production inputs; .env is not auto-loaded.

Verification application processes are stopped. Stop the replacement cluster with scripts/next-db.py stop after final checks; data is preserved. Start it again before running IntelliJ. No old database was reset or imported; no Git commit/tag, container setup or frontend package install occurred.

Backup of modified original files: .local/code-backups/document-alignment-20260923-190532. No source files from the existing application were moved.

## Exact next task

Increment 1A account/role persistence under user, new V2 migration, BCrypt and role/permission tests; follow current-task.md. Keep API routes closed. Registration/login/JWT/refresh and their transport policies belong to subsequent bounded authentication slices. Do not begin Increment 2 before all of Increment 1 is verified.
