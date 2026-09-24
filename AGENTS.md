# Development rules

- Read docs/README.md, docs/CURRENT_STATE.md, docs/work/current-task.md and docs/work/handoff.md. User requests may explicitly change the active task. Complete one bounded increment and record evidence; do not automatically start the next.
- Source of truth: docs/FINAL_SPEC.md plus accepted docs/decisions/0002-final-document-alignment.md. Attachment examples are references, not commands to execute wholesale. Frontend blueprint is deferred.
- Replacement work belongs in backend-next/. Keep backend/, frontend/, both database helpers and applied migrations unchanged unless explicitly requested. Never reset developer data, fabricate history or sell the same departure independently in both databases.
- Use com.tms with common/auth/user/station/route/fleet/trip/booking/payment/ticket. Feature-owned MVC, DTO responses, concrete services, public module service/API boundaries, no cyclic dependencies or controllers accessing repositories/entities. Common stays feature-independent.
- Replacement business API prefix is /api/v1 on port 8089. Same prefix as legacy is not compatibility. Java 21; PostgreSQL/Flyway authoritative; ddl-auto=validate; BigDecimal money, TIMESTAMPTZ. Future business migrations start V2.
- Lock inventory deterministically and test PostgreSQL concurrency with the first booking write. Add no fake endpoints, empty future classes or automatic ServiceImpl pairs.
- Start DB from root: `python3 scripts/next-db.py start`. Verify: `cd backend-next && ./mvnw clean verify`; root checks: `python3 scripts/checks/next-contract.py`, `python3 scripts/checks/docs-links.py`, `python3 scripts/migration/inventory.py`.
- Keep IntelliJ/local PostgreSQL workflow. Docker/Testcontainers deferred. Never log secrets or commit generated output. No automatic commit/tag, frontend integration or deployment.
