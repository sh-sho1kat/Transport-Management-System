# Next task: Increment 1A — account and role persistence

## Objective
Implement the first bounded slice of specification Increment 1. Persist accounts and roles; expose no authentication endpoints yet.

## Relevant specification sections
FINAL_SPEC sections 5 (roles), 14–15 (user model and booking relationship), 50–52 (identity schema), 95–96 (security/authorization), 100 (Increment 1). Use actual field definitions and ADR 0002 when interpreting examples. Complete refresh-token persistence/lifecycle in the subsequent authentication slice.

## Allowed modules
backend-next user entity/repository/service/policy and matching tests; common security interfaces only if needed. New Flyway migrations and supporting contract/docs are allowed. Existing backend, frontend and applied migrations are read-only.

## Required API/database changes
Add V2 users/user_roles, BIGINT identity and the documented fields/constraints, BCrypt hashing, account normalization and a reviewed named permission matrix. Define public user service boundaries without leaking entities. No login, registration or token endpoint in this slice; non-health requests stay denied.

## Acceptance tests
Real PostgreSQL migration/constraint tests, password hashing, duplicate-account rejection, account status and role validation, default-denied grants, audit timestamps, and full foundation/architecture suite. Resolve phone/email normalization in this slice against the specification before implementation.

## Explicit exclusions
JWT/refresh endpoints, other business entities, frontend changes, migration/import, Docker and production deployment.

## Following slice
Increment 1B completes registration/login/JWT and a reviewed refresh-token transport/lifecycle before exposing authentication; split further if necessary. Do not proceed automatically. All Increment 1 acceptance must pass before Increment 2.
