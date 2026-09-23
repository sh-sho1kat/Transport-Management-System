> Updated: counter staff, walk-in tickets, cash collection/refunds, and fare updates are documented in [Counter operations](docs/COUNTER-OPERATIONS.md).

# Run locally: IntelliJ + npm + DBeaver + Postman

Docker files have been removed. This setup runs directly on your machine. The steps below target your Linux environment.

## Ports and credentials

| Service/account | Address or username | Password |
|---|---|---|
| PostgreSQL | `127.0.0.1:55432`, database `wayline`, user `wayline` | `WaylineDb123!` |
| Backend API | `http://localhost:8088` | Session login below |
| Frontend | `http://localhost:5178` | Use one of the app accounts below |
| Administrator | `admin@example.test` | `AdminPass123!` |
| Passenger | `passenger.demo@example.test` | `DemoPass123!` |
| Driver | `driver.demo@example.test` | `DemoPass123!` |

These are explicit **local development credentials**. Application accounts are separate from the database user and your Linux password. The local profile creates accounts only when absent; it never changes an existing password. This new setup uses its own PostgreSQL data directory, not your older database on port 5432.

## 1. Start PostgreSQL

Install PostgreSQL server and Python 3 if not already installed. On Ubuntu:

```bash
sudo apt install postgresql postgresql-contrib python3
```

From this project's root directory (the folder containing `backend`, `frontend`, and `scripts`):

```bash
python3 scripts/local-db.py start
```

Run that command as your normal user, **without sudo**. It creates an isolated cluster under `.local/postgres`, starts it on port **55432**, creates database `wayline`, and enables `btree_gist`. It does not change the system PostgreSQL port, users, or databases. Database files are excluded from source releases.

After a reboot, run the same start command again. It preserves data. To stop only this project's database:

```bash
python3 scripts/local-db.py stop
```

A port conflict causes startup to fail rather than choosing another port. See `.local/postgres.log`. If PostgreSQL is installed in a custom location, set `POSTGRES_BIN` to its `bin` directory. Do not delete `.local` to fix a login error; it contains your data.

## 2. Backend: IntelliJ Run button

1. In IntelliJ, open **`backend/pom.xml`** as a project and let Maven import finish.
2. Set Project SDK and Maven importer/runner JDK to **JDK 21 or newer**. A JRE alone is insufficient.
3. Open `src/main/java/com/tms/TmsApplication.java`.
4. Click the green Run triangle beside `main()` → **Run TmsApplication**.
5. Wait for `Tomcat started on port 8088`.

No `.env`, VM options, Docker, or manually copied classpath are required. `spring.profiles.default=local` automatically loads `application-local.properties` from the classpath regardless of IntelliJ's working directory. Remove old environment overrides (`PORT`, `DATABASE_*`, `SPRING_PROFILES_ACTIVE`, bootstrap credentials) from an existing Run configuration if they point at an older setup.

Flyway creates and upgrades the tables. Hibernate validates them. The local profile provisions the three accounts in the table above. The admin default display name is Administrator.

Verify: open **http://localhost:8088/actuator/health**. Expected: `{"status":"UP"}` (additional probe groups may also appear). Opening `/` on the backend is not the frontend and is not a health check.

IntelliJ reference: https://www.jetbrains.com/help/idea/running-applications.html

## 3. Frontend: npm run dev

In a separate terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open **http://localhost:5178**. Vite uses strict port 5178 and forwards `/api` to backend port 8088. Both `localhost:5178` and `127.0.0.1:5178` are allowed for local CORS; use one hostname consistently to retain cookies.

The backend must be running first. No frontend password, DB connection string or secret is needed.

## 4. Add fictional trips (optional, useful for Postman)

From the project root, with the backend running:

```bash
python3 scripts/seed-demo.py
```

This adds one coach, two stops, a route and three upcoming trips. It reuses existing demo accounts/resources and does not reset passwords. Then sign in with any listed app account.

## 5. Connect DBeaver

New Database Connection → **PostgreSQL**, then enter:

```text
Host:       127.0.0.1
Port:       55432
Database:   wayline
Username:   wayline
Password:   WaylineDb123!
JDBC URL:   jdbc:postgresql://127.0.0.1:55432/wayline
SSL:        disabled for this loopback-only development cluster
```

Click **Test Connection**; download the PostgreSQL JDBC driver if prompted. Save the connection. Expand `wayline → Schemas → public → Tables`. Refresh after the backend's first startup.

Run in a SQL editor:

```sql
SELECT current_database(), current_user, inet_server_port();
SELECT email, role, active FROM accounts ORDER BY role;
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

Expected database/user/port: **wayline / wayline / 55432**. Do not use the app administrator email as the database username. Account passwords are hashed; they are not readable from the accounts table.

DBeaver reference: https://dbeaver.com/docs/dbeaver/Database-driver-PostgreSQL/

## 6. Check Postman

Import **`postman/Wayline-Local.postman_collection.json`**. Use Postman Desktop, or the desktop agent for localhost access. Leave the cookie jar enabled. No Bearer token is required.

The collection has baseUrl `http://localhost:8088`. It automatically fetches a fresh CSRF token before each POST/PATCH/DELETE and keeps the session cookie, including after login rotates it. Run its requests in order after seeding:

1. Health → passenger login → current account.
2. Search trips → seats → hold → confirm → history → ticket → cancellation.
3. Administrator login → fleet, staff, routes, reports and activity log.
4. Driver login → assigned trips/manifest → expected 403 for administrator fleet.

The search/seat/hold/confirm responses set `tripId`, `seatNo`, `holdId`, `bookingKey`, and `bookingId` automatically. Re-send Confirm before cancelling to see the same booking returned with HTTP 200 instead of 201. Login again to switch roles. A 403 on a permitted mutation usually means cookies or CSRF were disabled; do not remove backend security to work around it.

For manual requests: GET `/api/v1/auth/csrf`, send its token as `X-CSRF-TOKEN`, keep the same JSESSIONID cookie, POST login, then fetch a new CSRF token before the next mutation. Login body:

```json
{"email":"admin@example.test","password":"AdminPass123!"}
```

Postman references: https://learning.postman.com/docs/tests-and-scripts/write-scripts/postman-sandbox-reference/pm-send-request and https://learning.postman.com/docs/use/send-requests/response-data/cookies

## 7. Check everything together

With PostgreSQL, backend and frontend running, and demo trips seeded:

```bash
python3 scripts/check-connectivity.py
```

It verifies health, all three app logins, sessions, hold/confirmation/replay/cancellation, role denial, frontend HTML and frontend-to-backend proxy connectivity. It creates and cancels one fictional booking. Database password authentication is also tested by `local-db.py start` using PostgreSQL's command-line client.

## Important differences from the previous release

- Use **8088 / 5178 / 55432**, not 8000 / 5173 / 5432.
- Old `.env` files are no longer automatically loaded. The local profile is the one source of development defaults.
- Docker Compose, Dockerfiles and container-specific Nginx configuration are removed.
- Email is disabled locally. Real SMTP and password-recovery delivery need separate configuration.
- Do not activate the local profile on a public server; it intentionally provisions documented development accounts.
