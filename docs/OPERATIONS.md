# Local operations

See [START-HERE.md](../START-HERE.md) for exact credentials and connection instructions.

## Profiles and configuration

`spring.profiles.default=local` activates classpath-based local settings without depending on IntelliJ's working directory. No `.env` files are imported. `application-local.properties` sets the backend to loopback port 8088, PostgreSQL to loopback port 55432, allowed frontend origins to localhost/127.0.0.1:5178, and the initial development administrator. The local-only `LocalAccounts` initializer adds fictional passenger/driver accounts when absent.

Environment variables such as PORT, DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, BOOTSTRAP_ADMIN_EMAIL/PASSWORD, ALLOWED_ORIGINS and FRONTEND_URL can override defaults. Remove stale overrides from IntelliJ Run configurations if the application connects to an old port or database. Explicitly activating another Spring profile disables the local demo-account initializer.

The local database helper creates an independent PostgreSQL cluster in `.local/postgres`, with SCRAM password authentication and TCP access bound only to 127.0.0.1. Its `wayline` role owns that entire development cluster. It does not alter the operating system's existing PostgreSQL cluster. Run it as a normal user, not root. Stop it with `python3 scripts/local-db.py stop`; restart after reboot with `start`. Never delete its data directory to troubleshoot.

## Database and migrations

Flyway V1 creates relational tables/constraints; V2 adds indexes for foreign-key lookup paths. Hibernate uses `ddl-auto=validate`. Do not edit applied migrations; add a new version. The trusted `btree_gist` extension supplies bus/driver overlap constraints.

DBeaver uses the same PostgreSQL JDBC endpoint as Spring Boot. Refresh its table tree after Flyway has run. Do not edit authentication hashes or live trip inventory manually. Catalog and reservation changes should go through the role-checked API.

## Backup

Use your configured DBeaver connection's PostgreSQL backup facility, or the installed `pg_dump` client. With a protected password file/PGPASSFILE configured, run:

```bash
mkdir -p backups
chmod 700 backups
umask 077
pg_dump -h 127.0.0.1 -p 55432 -U wayline -d wayline -Fc -f backups/wayline.dump
```

Keep backups private and test restoring into a different empty database. Do not overwrite a running database as a restore test. `.local/` and `backups/` are excluded from release archives.

## Sessions, email and limits

Sessions and rate limits live in the backend process. Restarting signs users out; it does not erase bookings. Login rotates the session cookie. State-changing API calls require CSRF, including Postman calls; the supplied collection handles it.

The local profile disables mail. Password recovery does not deliver messages until a real SMTP server is configured; no mail container is included. The app records cash collected or refunded outside the application; it does not process electronic payments. Online reservations start unpaid. See [Counter operations](COUNTER-OPERATIONS.md) for collection and refund controls.

Default policy: 5-minute holds, 4 seats, 2-hour cancellation cutoff, 30-minute turnaround. Startup accepts hold duration 1–60 minutes, seat limit 1–4, cutoff 0–168 hours, turnaround 0–1440 minutes. The UI displays the standard four-seat limit; a lower server limit is enforced with an error.

## Health and troubleshooting

- Backend: http://localhost:8088/actuator/health — UP once the database is reachable.
- Frontend: http://localhost:5178 — backend must also be running for API calls.
- Database: `python3 scripts/local-db.py status`; startup log `.local/postgres.log`.
- Port conflicts fail explicitly. Do not silently change one port without updating the proxy, CORS and JDBC configuration.
- Wrong credentials on an existing database: bootstrap never resets stored passwords. The documented defaults apply to a fresh local cluster; use a reviewed reset if you intentionally changed them.
- Postman: keep cookies enabled; sign in for the intended role. DRIVER and PASSENGER should receive 403 on administrator endpoints.

The local profile's documented passwords and demo accounts are for development only. Before any public deployment use separate secrets, HTTPS/Secure cookies, restricted database credentials, an explicit non-local profile, backup/restore procedures, and appropriate SMTP/session infrastructure.
