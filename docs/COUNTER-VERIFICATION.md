# Counter feature verification — 2026-09-23

Applied directly to this IntelliJ project. Original edited files were backed up under `.local/code-backups/counter-features-20260923-154251/`.

- PostgreSQL integration suite: **27 tests, 0 failures, 0 errors, 0 skipped**. Includes eight new tests for accountless/idempotent counter sales, seat availability and stale fares, departure cutoff, payment permissions/stale writes, explicit refunds, preserved quoted fares, HTTP validation/CSRF/roles, and competing online/counter claims.
- Tested from this project's backend with a separate temporary PostgreSQL 16 database `wayline_counter_test` on port 55441. Existing application data was not used by the tests.
- Flyway V1/V2/V3 initialized successfully on isolated PostgreSQL. V1/V2 were not edited; V3 upgrades existing installations when the backend restarts.
- Frontend production build and Prettier formatting checks passed.
- OpenAPI check passed: 49 operations, 43 schemas.
- Browser skill verification on a separate preview database: counter login, role-specific navigation, walk-in seat selection and sale with no email/account, manifest persistence, UNPAID → PAID collection with a note, and ticket display showing PAID / CASH_COUNTER. Print stylesheet uses the existing `ticket-print` class; no physical printer was invoked.
- Role restrictions, assigned driver boarding collection, cancellation/refund transitions and fare updates were exercised by PostgreSQL-backed integration tests.

The application database on port 55432 was not cleared or reseeded. Restart the backend in IntelliJ to activate the migration and create the local counter account. Ports and existing passwords are unchanged.
