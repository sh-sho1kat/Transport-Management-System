# Publish the CV demo: Render + Neon

This deploys the **working `backend/` and `frontend/`** together at one HTTPS address. `backend-next/` remains an unfinished, independent replacement. Your IntelliJ and `npm run dev` setup is unchanged. Docker is needed by Render only; you do not need to install it locally.

## 1. Neon

1. Open your Wayline project. Use a database dedicated to this fictional demo; do not point it at real passenger data.
2. In **Connect**, choose database `neondb` and role `neondb_owner` (or your dedicated demo database/role).
3. Reset the password you previously pasted into chat. Keep the new password private.
4. Turn **Connection pooling off**. This application already uses a small connection pool and Flyway runs migrations through the same connection.
5. Copy the hostname, database name, role and new password separately. No local database upload is necessary: startup applies the existing migrations and creates fictional demo records.

For the endpoint you supplied, the direct JDBC URL is:

```text
jdbc:postgresql://ep-noisy-sea-b4j91zmx.c-6.us-east-2.aws.neon.tech:5432/neondb?sslmode=require&connectTimeout=30
```

Confirm the hostname against your current Neon Connect dialog. `DATABASE_URL` must start with **jdbc:postgresql://**, without `username:password@`. Credentials go in separate variables below. Do not paste a `psql` command. The psql option `channel_binding` is not used in this JDBC URL.

## 2. Render: use your existing New Web Service screen

Select the GitHub repository `sh-sho1kat/Transport-Management-System`.

| Setting | Value |
|---|---|
| Name | `wayline-demo` (or another available name) |
| Language / Runtime | **Docker**, not Node |
| Branch | `main` |
| Region | Ohio, close to your Neon region |
| Root Directory | **Leave blank** |
| Dockerfile Path | `./Dockerfile` |
| Docker Build Context | `.` (repository root), if shown |
| Docker Command | Leave blank; the Dockerfile supplies it |
| Instance Type | **Free — $0** |
| Health Check Path (Advanced) | `/actuator/health` |

Do not enter `yarn`, `yarn start`, or a separate frontend build command. Selecting Docker replaces those Node settings. The image builds React, bundles it into the Spring Boot application, then runs Java 21 as a non-root user. No separate frontend service is required.

Add these environment variables:

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `production` |
| `DEMO_ENABLED` | `true` |
| `SERVER_ADDRESS` | `0.0.0.0` |
| `PORT` | `10000` |
| `DB_POOL_SIZE` | `3` |
| `DATABASE_URL` | The JDBC URL from step 1 |
| `DATABASE_USER` | `neondb_owner` (or your selected role) |
| `DATABASE_PASSWORD` | Your **new private Neon password** |
| `FRONTEND_URL` | `https://YOUR-SERVICE.onrender.com` |
| `ALLOWED_ORIGINS` | The exact same HTTPS URL, without a trailing slash |
| `BOOTSTRAP_ADMIN_EMAIL` | A private owner login, e.g. `owner@example.test` |
| `BOOTSTRAP_ADMIN_PASSWORD` | A unique private password, 10–72 UTF-8 bytes |
| `MAIL_ENABLED` | `false` |

Use the actual address Render assigns, not a guessed name. If the address appears only after creation, update both URL variables under **Environment** and redeploy. Never put database or private owner credentials into frontend variables or GitHub. The private owner account is separate from the public admin preview.

Click **Deploy Web Service**. Wait for the build and startup logs to complete and the service to become live. Free-plan builds/cold starts can be slow. The first startup creates tables, public accounts and seven upcoming fictional journeys. If a build fails, inspect the first error in Logs rather than changing the runtime to Node.

Alternative: **New → Blueprint** can read the included `render.yaml` and ask for the private values. Use either the manual service or the Blueprint, not both.

## 3. Public accounts shown on the website

| Account | Email | Password | Access |
|---|---|---|---|
| Passenger | `passenger.demo@example.test` | `DemoPass123!` | Search, hold seats, book and cancel fictional journeys |
| Administrator preview | `admin.demo@example.test` | `DemoAdmin123!` | Read management screens; server blocks writes |

Both appear on the signed-out frontend with one-click login buttons. Private owner credentials never appear there. Shared demo identity, password and account status changes are blocked. Admin action buttons can remain visible to demonstrate the interface, but submission is rejected with a read-only message.

`VITE_PUBLIC_DEMO=true` is set during the Docker build; `DEMO_ENABLED=true` separately enables backend seeding and protections. Both are off in ordinary local development. Deploy only fictional data here. Bookings are shared across visitors, so do not enter personal information or real payment details. This is a public preview, not production hosting.

## 4. Verify after deployment

1. Open `https://YOUR-SERVICE.onrender.com/actuator/health` in your browser or Postman: expect HTTP 200 and `{"status":"UP"}` (additional health probe groups may appear).
2. Open the main address. Confirm both demo buttons and credentials are visible.
3. Try the passenger account. Search Dhaka → Chattogram for tomorrow or one of the next seven dates in Asia/Dhaka. Select a seat, book, view the ticket, then cancel the fictional booking.
4. Log out. Try the admin preview. View trips/staff/reports. An attempted edit should return HTTP 403 with `DEMO_READ_ONLY`.
5. In a private browser window, log in using your private owner credentials if you want full management access.
6. Redeploy once and verify existing records remain and matching demo departures are not duplicated.

For Postman authenticated requests: first `GET /api/v1/auth/csrf`; retain cookies, copy the returned token into `X-CSRF-TOKEN`, then `POST /api/v1/auth/login` with JSON email/password. Fetch a **new** CSRF token after login before further writes. Keep the same cookie jar. Admin preview writes remain forbidden even with a valid token.

DBeaver: PostgreSQL connection, direct Neon host above, port `5432`, database `neondb`, user `neondb_owner`, new Neon password; enable SSL with mode `require`. Test Connection. This is separate from your local port 55432 database.

## 5. Sleep, wake-up and demo maintenance

Render Free automatically sleeps after 15 minutes without inbound traffic and wakes on a visit; its loading page can appear while startup completes. No scheduled pings or keep-alive service are required. Put a note beside your CV link: **“Live demo — first load may take about a minute.”** Spring Boot/database startup can add time. See [Render free-service behavior and limits](https://render.com/docs/free).

Startup adds missing departures for the next seven days without deleting existing data. There is **no automatic reset**: shared bookings persist. If continuously active for over a week, redeploy to refresh upcoming dates. Sessions are in memory, so visitors sign in again after restart. Email recovery is disabled because SMTP is not configured. Payment figures represent demo cash tracking, not a real payment gateway.

Free tiers have usage limits and may suspend service when exhausted; monitor both dashboards. Avoid upgrading or enabling paid resources just for this setup. Render’s filesystem is temporary; PostgreSQL data lives in Neon. See [Render Docker deployment settings](https://render.com/docs/blueprint-spec) and [Neon connection documentation](https://neon.com/docs/connect/connect-from-any-app).

## Troubleshooting

- **Driver rejects DATABASE_URL:** use the JDBC URL, not the complete `postgresql://user:password@...` string.
- **Database authentication failed:** verify the newly reset password and role; save updated Render variables and redeploy.
- **Database connection/migration timeout:** use the direct hostname, confirm Neon is available, then retry deployment. Do not delete Flyway history or change applied migrations.
- **Login/CSRF fails:** open the HTTPS service address and use exact matching `ALLOWED_ORIGINS`/`FRONTEND_URL`; refresh before retrying. Keep secure cookies enabled in the cloud.
- **No demo cards:** ensure the root Dockerfile is selected; it builds the frontend with the demo flag.
- **No future departures:** restart/redeploy to seed the upcoming dates; inspect startup errors if missing.
- **Out of memory/build failure:** inspect Render logs; the runtime caps heap to 60% of container memory, but hosted resource behavior must be checked on the Free instance.

Local verification evidence and cloud limitations are recorded in [deployment handoff](work/live-demo-handoff.md). No Render/Neon dashboard deployment has been performed by this task.
