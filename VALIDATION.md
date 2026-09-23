# Verification — Local Development Edition

Verified on 23 September 2026 with fictional, isolated data. No existing system PostgreSQL database was changed.

## Results

| Check | Result |
|---|---|
| PostgreSQL password authentication | Passed: wayline / wayline on 127.0.0.1:55432 |
| Flyway V1 and V2 migrations | Both successful |
| Default backend startup, no environment overrides | Passed: local profile, port 8088 |
| Frontend npm development server | Passed: strict port 5178 |
| Health without an SMTP server | UP; optional email correctly excluded |
| Administrator, passenger and driver credentials | All three logins and authenticated sessions passed |
| HTTP connectivity script | 9 checks passed |
| Postman collection via Newman 6.2.1 | 23 assertions passed, zero failures; 32 HTTP requests including CSRF fetches |
| PostgreSQL integration suite | 19 tests passed, zero failures/errors/skips |
| Frontend production build | Passed |
| OpenAPI contract check | 44 operations and 40 schemas verified |
| Frontend source formatting | Passed |

## What was exercised

The connectivity script verifies backend/database health, all listed account passwords, owned seat holds, confirmation, identical retry, cancellation, passenger denial from admin endpoints, frontend HTML, and frontend proxy authentication including CSRF.

The actual supplied Postman collection was run with Newman's official runner. Its callback-based pre-request script fetches CSRF using the cookie jar before mutations. Passenger booking/ticket/cancellation, administrator catalog/reporting, driver manifest and expected role denial passed. This checks the importable artifact, not just equivalent hand-written requests. The Postman desktop UI itself was not automated.

The 19-test integration suite uses real PostgreSQL 16.15. It covers ownership, roles, CSRF/CORS, request validation, single-use password reset and session revocation, staff deactivation, multi-seat expiry/atomicity, idempotent confirmation, cancellation cutoffs/override/audit, publication conflicts, immutable trips, rollback after a forced final-write failure, 50 synchronized seat contenders with exactly one winner, simultaneous confirmations, simultaneous publications, operator cancellation racing confirmation, and health with SMTP disabled.

Runtime/build used Maven 3.9.11, JDK 25 with Java 21 release target, Spring Boot 3.5.16, Node 18.19.1 and Vite 6.4.1. Java 21+ and Node 22 are recommended in the guide. No hosted CI execution is claimed.

## Limits

IntelliJ and DBeaver desktop windows were not directly driven. The backend was started with the same main application/default profile and no environment overrides, while the exact DBeaver database host/port/user/password were verified using PostgreSQL password authentication and Spring's JDBC connection. Actual IDE/driver installation remains machine-specific.

No Docker tooling is needed or included. Local email is disabled; real SMTP delivery, production HTTPS, deployment, GPS and real payments are outside this setup. The 50-caller test is a correctness test, not a performance benchmark. Tests use only the separate wayline_test database, which they truncate. Never point them at application data.

The connectivity script creates and cancels one fictional booking; these rows remain as audit/history evidence. The local database directory, session cookies and raw runner responses are excluded from the ZIP. Check release/verification for sanitized result summaries.
