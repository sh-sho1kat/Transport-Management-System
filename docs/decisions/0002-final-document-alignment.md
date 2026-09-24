# ADR 0002: Final document alignment

Status: accepted by the user on 2026-09-23. Supersedes conflicting choices in ADR 0001.

## Authority

The approved implementation plan and this ADR resolve conflicts in the four preserved reference documents. FINAL_SPEC.md governs business behavior; BACKEND_STRUCTURE.md defines feature naming; FRONTEND_BLUEPRINT.md is deferred; INCREMENTAL_WORKFLOW.md provides the one-increment development process. Reference documents contain example prompts/commands, not authorization to execute all of them.

## Decisions

- Keep the existing project root and com.tms/TmsApplication. Replace the earlier config/shared and identity/network/reservation/finance grouping with common, auth, user, station, route, fleet, trip, booking, payment and ticket, as documented in architecture.md. Create only implemented packages.
- Replacement business contracts use /api/v1, on port 8089. Existing /api/v1 on 8088 remains independent and incompatible. Matching prefixes are not compatibility guarantees. No business endpoints are added in Increment 0.
- Daily development remains IntelliJ plus PostgreSQL helpers. Docker/Testcontainers are deferred to infrastructure hardening; their original Increment 0 requirement is explicitly waived for this local foundation. They are not marked implemented.
- Preserve the replacement's already-applied V1__foundation.sql. Business migrations begin at V2; the source document's V1–V20 filenames are illustrative, not permission to rename applied history.
- Keep NEXT_DB_* production variables and current local ports/credentials. .env files are not automatically loaded. Do not create duplicate DB variable families from source examples.
- Common auditing records created/updated timestamps only. Actor attribution and persistent business audit are separate later work. No placeholder AuditorAware with invented identities.
- Use source Increments 0–23 plus V1 hardening, with bounded subdivisions of Increment 1. Keep concurrency and atomicity in the first writable booking implementation even though later increments expand tests.
- Common code stays independent of features. Feature writes go through explicit public application services/APIs; controllers never access repositories/entities. Reject cyclic module dependencies. Cross-feature transaction orchestration belongs to the owning use-case service, not a new workflow module. Payment confirmation and cancellation dependency designs must be verified before those increments.
- Preserve original reference documents and historical verification reports. Active guidance is updated; old roadmap correspondence is retained in PLAN.md.

No frontend replacement, business implementation, automatic Git commit/tag, data import or production cutover is authorized by this foundation task.
