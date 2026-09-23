# Cutover gate (future work)

No cutover is authorized or performed by the Stage 0 scaffold.

1. Complete feature acceptance, authorization, concurrency, payment, expiry, cancellation and migration tests on PostgreSQL.
2. Adapt frontend against implemented v2 contracts; verify login/refresh, CORS, ownership, counter cash operations, ticket/check-in and errors end to end.
3. Rehearse import and reconciliation from an immutable backup; document archive access, ID mappings and rejected rows.
4. Schedule a maintenance window; stop legacy sales and background inventory writers. Snapshot the source and resolve/expire outstanding holds under the approved policy.
5. Import into the replacement, reconcile counts, money and active inventory; obtain release approval on the concrete result.
6. Switch frontend/service routing once. Keep legacy history read-only. Never allow independent sales in both services for the same departure.
7. Monitor health, financial reconciliation and failed jobs. Before any replacement sale, rollback may restore routing to the frozen old state. After replacement writes, rollback requires reconciled reverse migration or a forward fix; never just re-enable old inventory.
8. Once stable, rename backend-next to backend and archive the old backend/history under the agreed retention policy. Recheck all setup, CI and documentation paths.

Production readiness also requires TLS, managed secrets, restricted DB roles, backups/restore drills, gateway verification, rate limits, observability, retention policies and operational ownership. A production profile alone does not satisfy these gates.
