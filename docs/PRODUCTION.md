# Production foundation and deployment prerequisites

The refactor improves maintainability and adds safer production configuration. It does not make an unconfigured local application production-ready.

## Profiles

Local development is unchanged: start the private PostgreSQL instance, use IntelliJ Run on TmsApplication, and use `npm run dev` for the frontend. Local demo accounts exist only under the local profile.

For deployment, explicitly set `SPRING_PROFILES_ACTIVE=production`. Supply `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `ALLOWED_ORIGINS` (exact origins) and `FRONTEND_URL`. Keep secrets in the deployment's secret store. `DB_POOL_SIZE` defaults to 10 and should be sized against PostgreSQL capacity and replica count. Use a least-privilege runtime database role and a separately authorized migration identity where operationally appropriate; the local helper's database superuser is for development.

Production enables secure session cookies and graceful shutdown. Serve the frontend/API over HTTPS with a correctly configured trusted reverse proxy. Do not activate local and production together. Admin bootstrap is optional and requires explicit BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD; remove bootstrap credentials after provisioning. Local sample passwords must never be reused for deployment.

## Before real traffic

- Configure TLS, exact CORS origins, secret rotation and SMTP/password recovery.
- Deploy the built frontend, not the development server. Verify cookie/CSRF behavior through the actual proxy and domain.
- Test database migration upgrades, backups and restoration; monitor storage, pool saturation and lock waits.
- Load-test seat contention, manifest/report sizes and peak login volume. Measure before splitting services.
- Add central logging/metrics/alerts without exposing passenger data or session credentials.
- Test operational account provisioning, deactivation, incident response and payment correction/refund procedures.

## Before multiple backend replicas

Sessions and the rate limiter are in memory. Add a supported shared session store and centralized/edge rate limits before horizontal scaling. Review scheduled hold cleanup coordination and test concurrent execution. PostgreSQL constraints remain essential even with a cache. Add durable events/outbox processing when reliable asynchronous notifications or payment integrations are actually introduced.

Cash payment recording is not an electronic payment system. Financial reconciliation, partial payments and gateway integrations remain separate work.
