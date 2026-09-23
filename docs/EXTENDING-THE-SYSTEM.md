# Adding features and roles

## A new backend feature

1. Create `com.tms.<feature>` and only the layers it needs: controller, DTOs, service, entity/repository and mapper.
2. Define explicit request/response records in `dto/request` and `dto/response`. Keep entities private to persistence/application code.
3. Put orchestration and transaction boundaries in public service entry points. Use a service interface when another feature needs a stable API or multiple implementations are useful; avoid empty interface/implementation pairs for every small class.
4. Reuse shared pagination, error handling, validated settings and identity permissions. Do not put feature-specific rules in `shared`.
5. Call another feature's service for behavior. Direct repository access across features is an explicit coupling decision, appropriate for coordinated transactions or read projections; document it rather than hiding it behind generic utilities.
6. Add a new Flyway migration for persistent changes. Never modify an already-applied migration. Design additive, backward-compatible changes and test upgrades with representative data.
7. Add tests for authorization, ownership, failure rollback, concurrency where relevant, and HTTP behavior. For tests using the shared PostgreSQL fixture, use a dedicated disposable test database: the fixture truncates tables.
8. Generate the API contract with `python3 scripts/openapi.py`, inspect it, and run `--check`. The generator deliberately supports this codebase's DTO/controller conventions; extend it before using unsupported annotation/signature patterns.

## A new role

1. Add the role to `identity/domain/Role.java`.
2. Add only the necessary grants in `identity/security/RolePermissions.java`. Add a Permission when it represents a new business capability, not merely a new role name.
3. Add a Flyway migration extending PostgreSQL's accounts role constraint. Keep existing enum strings stable.
4. Run `python3 scripts/permissions.py`. Staff provisioning choices derive from those generated grants; do not duplicate role lists in components.
5. Review resource scope and endpoint entry policies. A custom trip operator, for example, needs appropriate catalog/driver read access in addition to trip editing; do not grant staff administration just to make a dropdown load. Add dedicated read capabilities/endpoints when needed.
6. Add positive and negative API tests and frontend access tests. New roles are not dynamically editable database permission bundles; they are deliberately reviewed code/configuration changes.
7. If introducing role changes on existing accounts, increment authVersion and require a fresh session so old authority grants cannot survive the change.

## A new frontend feature

1. Add its pages/components under `src/features/<feature>`.
2. Use the shared HTTP client for cookies, CSRF, timeout and error handling. Keep large workflows in focused components/hooks.
3. Register the page, label, icon, visibility and lazy component in `app/features.jsx`.
4. Use `can(user, permission)` for controls. Resource state/ownership can additionally restrict actions; the backend still decides access.
5. Keep generic controls in `shared/ui`; keep business controls inside the owning feature. Avoid cross-feature page imports.
6. Run `npm test`, `npm run build`, and `npm run format:check`.

## Checks

From the root: `python3 scripts/openapi.py --check` and `python3 scripts/permissions.py --check`.
From frontend: `npm test`, `npm run build`, `npm run format:check`.
From backend: `./mvnw test` with TEST_DATABASE_URL, TEST_DATABASE_USER and TEST_DATABASE_PASSWORD pointing at a **separate test database**. For architecture/policy checks only, no database is required: `./mvnw -Dtest=ArchitectureTest,PermissionPolicyTest test`.

CI runs these checks. Source-level architecture tests enforce MVC imports and permission usage; they intentionally allow the current cross-feature JPA relationships.
