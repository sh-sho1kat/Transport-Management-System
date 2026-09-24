# Documentation index

## Start here

- [Replacement setup](../backend-next/README.md): IntelliJ, PostgreSQL, DBeaver, Postman and verification.
- [Current state](CURRENT_STATE.md): implemented, partial, missing and deferred requirements.
- [Plan](PLAN.md): specification Increments 0–23 and release hardening.
- [Current task](work/current-task.md) and [verified handoff](work/handoff.md).

## Authoritative targets and decisions

- [FINAL_SPEC](FINAL_SPEC.md): complete source specification, preserved verbatim.
- [Backend structure reference](references/BACKEND_STRUCTURE.md).
- [Future frontend blueprint](references/FRONTEND_BLUEPRINT.md): deferred; no frontend dependencies installed.
- [Incremental workflow reference](references/INCREMENTAL_WORKFLOW.md).
- [Accepted alignment decision](decisions/0002-final-document-alignment.md): precedence, local-only setup, package names, API version and migration numbering exceptions.
- [Architecture](architecture.md), [database relationships](database.md), [permissions](permissions.md).

## Compatibility and migration

- [API compatibility matrix](API_COMPATIBILITY.md), [migration map](MIGRATION_MAP.md).
- [Existing contract](api/current-v1.md), [replacement implemented contract](api/next.yaml).
- [Compatibility boundary](migration/compatibility.md), [data reconciliation](migration/data-mapping.md), [cutover gates](migration/cutover.md).

## Existing working application

[Local run guide](../START-HERE.md), [existing architecture](../ARCHITECTURE.md), [existing database](DATABASE.md), [existing API](API.md), [counter operations](COUNTER-OPERATIONS.md).

Original references describe the eventual product. They do not mean those features currently exist. Historical reports retain their original facts; CURRENT_STATE and the handoff describe today's replacement.
