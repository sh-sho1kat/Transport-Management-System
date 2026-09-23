# Next task: Stage 1A — accounts and role persistence

## Objective
Implement the identity persistence foundation as one bounded slice, before issuing any tokens or exposing login endpoints.

## Relevant specification sections
Read the User, UserRole, account status, role, password and identity schema sections of ../specification.md. Follow ../permissions.md and ADR 0001 for migration precedence. Confirm exact fields/statuses from the source before creating DDL.

## Allowed modules
backend-next identity/user and identity/security, shared types only where required, new Flyway migrations, matching tests and documentation.

## Required API/database changes
Add V2 for users/roles with BIGINT IDs, unique normalized account identifiers, valid role constraints and BCrypt password storage. Define a permission vocabulary and explicit target role matrix. No public registration or login endpoint in this slice. Keep non-health routes denied.

## Acceptance tests
Real PostgreSQL migration/constraint tests; account normalization and password hashing; rejects duplicate identifiers and invalid roles; default grants fail closed; existing foundation and architecture tests pass. Maintain the rule preventing business modules from depending on workflow as features appear.

## Explicit exclusions
JWT issuance, refresh lifecycle, controller endpoints, frontend changes, driver records, business inventory, legacy data import and any edits to existing backend/migrations.

## Following slice
Stage 1B: registration/login plus JWT validation and a reviewed refresh transport/lifecycle, then endpoint/resource authorization tests. Split further if necessary; never expose partial insecure auth.
