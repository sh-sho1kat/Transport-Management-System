# Public demo hosting handoff — 2026-09-24

## Scope and changes

User requested a free CV demo, then chose to complete Render/Neon dashboards themselves. Added root Dockerfile and Render blueprint for the working backend/frontend. Added opt-in public passenger and read-only admin access, protected demo identities, and idempotent fictional catalog/seven-day departure initialization. Existing API/session/CSRF behavior is retained. Local development remains IntelliJ and npm; Docker is only the cloud packaging path.

The replacement roadmap and prior handoff remain historical/independent. No backend-next implementation, schema migration, database import or cloud deployment was performed. Next replacement task remains Increment 1A.

## Verification

- Java 21.0.12.1, PostgreSQL 16, isolated new database `wayline_live_test` on loopback port 55443.
- Backend `clean verify`: **39 tests, zero failures/errors/skips**. Five new tests cover public logins, seed repeatability, admin read-only protection/logout, passenger holds/profile protection, private admin writes/protected status, and protected password-reset tokens. Existing booking/concurrency tests pass.
- Frontend `npm test`: **4 passed**; `npm run format:check`: passed; `VITE_PUBLIC_DEMO=true npm run build`: passed.
- Built frontend assets copied into build output only and packaged into Spring Boot executable jar. Production profile started on isolated port 18088 with a separate disposable database `wayline_live_demo`, 300 MB heap. Health returned 200/UP, index and assets served. HTTP-only local verification overrides secure cookie to false; cloud production configuration retains secure cookies.
- Browser verified visible public credentials, seven departures, one-click admin login, read-only notice and logout. Passenger button also exercised.
- Real HTTP smoke check passed: session/CSRF lifecycle, admin read and mutation denial, passenger hold → booking → ticket → cancellation, and profile protection.
- Protected SHA-256 comparison: all 27 backend-next source/config/wrapper and applied current migration files unchanged. Existing local DB helpers untouched.

Commands used (from repository root, with a complete Java 21 toolchain):

```bash
TEST_DATABASE_URL=jdbc:postgresql://127.0.0.1:55443/wayline_live_test \
TEST_DATABASE_USER=wayline TEST_DATABASE_PASSWORD=wayline \
JAVA_HOME=/tmp/tms-align/jdk/usr/lib/jvm/java-21-openjdk-amd64 \
/tmp/apache-maven-3.9.11/bin/mvn -o -Dmaven.repo.local=/tmp/tms-m2 -f backend/pom.xml clean verify
cd frontend
npm test
npm run format:check
VITE_PUBLIC_DEMO=true npm run build
```

The isolated test fixture truncates its own database; never aim test commands at development or hosted data. Temporary evidence is under `/tmp/tms-live/` (`maven.log`, `package.log`, `runtime.log`). Source backups are under `.local/code-backups/public-demo-20260924/`.

## Remaining checks and limitations

Docker is not installed locally, so the actual container build remains unverified until Render builds it. Neon connectivity, Free-instance memory/startup performance, HTTPS cookie behavior and the public deployment URL must be checked after the user configures hosting. No cloud credentials were stored in the repository and the supplied Neon password was not used. Rotate that pasted password before deployment.

The public admin is intentionally read-only; private owner access enables management writes. There is no scheduled demo reset, real payment gateway, email delivery or uptime guarantee. Startup adds upcoming trips but preserves existing bookings. Sessions end on restart. See [complete dashboard instructions](../LIVE-DEMO.md).

Automatic approval review timed out on two local verification requests; each retry succeeded. No task action remains blocked by those timeouts.

## Next action

User: complete Neon and Render configuration in LIVE-DEMO.md, deploy from GitHub main, then perform the listed hosted verification. Do not start the unfinished backend-next as the public demo.
