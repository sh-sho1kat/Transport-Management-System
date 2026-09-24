# Executable development plan

Implement one bounded vertical slice per session. FINAL_SPEC uses Increments 0–30; this plan implements core 0–23 then V1 hardening. Later features stay deferred. Read exact named sections as well as cited ranges: a section can describe a related constraint, not a complete implementation recipe.

Only Increment 0 local foundation is completed by this alignment. All later increments are NOT STARTED. Original Docker/Testcontainers foundation requirements remain explicitly deferred under ADR 0002. Existing application code is reference material, not completion evidence for the replacement.

| Increment | Dependencies | Allowed module(s) | Specification sections | Deliverable | Acceptance |
|---|---|---|---|---|---|
| 0: Foundation | none | common | 99, 90, 132–138 | Profiles, Flyway, tracing/errors, timestamp auditing, health; local-first exception | Startup/restart; six-field errors; auditing; architecture; clean Java 21 verify |
| 1: Authentication | 0 | auth, user, common/security | 14, 50–52, 76–77, 95–96, 100 | 1A persistence/roles; 1B registration/login/JWT/refresh/logout; subsequent authorization slice if needed | Duplicate email/phone, BCrypt, invalid/expired JWT, refresh rotation/revocation, role and ownership denial |
| 2: Station | 1 | station | 16, 53, 78, 101 | Station vertical CRUD/status slice and migration | Unique code, validation, staff grants, public reads |
| 3: Route network | 2 | route | 17–18, 54–55, 79, 102 | Route and ordered stops with station references | Unique positive stop order, valid endpoints, route chain, no same origin/destination |
| 4: Seat layout | 1 | fleet/seatlayout | 19–20, 56–57, 80, 103 | Layout/seat definitions and management | Unique labels/coordinates, valid seat type/deck, capacity rules |
| 5: Bus | 4 | fleet/bus | 21, 58, 81, 104 | Bus records linked to layouts | Unique registration, layout FK, valid lifecycle |
| 6: Driver | 1 | fleet/driver | 22, 59, 82, 105 | Independent driver lifecycle | Employee/license uniqueness and validation; no login role implied |
| 7: Trip | 3, 5, 6 | trip, trip/driver | 23–25, 60–61, 83, 106 | Trips, driver assignments, scheduling policies | Invalid dates, overlapping bus/driver assignments, route/state checks |
| 8: TripSeat | 7 | trip/seat | 27, 63, 107 | Persist trip-specific seat snapshots | No duplicate generation; later layout edits do not mutate sold inventory |
| 9: TripFare | 7 | trip/fare | 26, 62, 108 | Segment and seat-type fares, server calculation | Invalid/reversed segments; missing fare; exact decimal arithmetic |
| 10: Trip search | 8, 9 | trip/search | 84, 109 | Station/date/segment query and DTOs | Stop order, scheduled trips, date/time boundaries, pagination |
| 11: Availability | 8, 9 | trip/seat, booking allocation read API as needed | 31–34, 85, 110 | Segment overlap policy and availability projection | Half-open adjacent reuse, overlapping HELD/CONFIRMED only; no writes |
| 12: Basic booking | 10, 11 | booking, booking/passenger, booking/allocation | 28–37, 64–66, 86–87, 111 | Pending booking/passengers/held allocations in one transaction, with locks and atomicity from day one | Same-seat conflict, invalid segment/passenger, rollback all allocations on failure; server fares |
| 13: Concurrency hardening | 12 | booking; trip/seat public locking service | 33–37, 112 | Expand deterministic locking and concurrency verification | 2 and 20 overlapping requests: exactly one succeeds; adjacent segments can both succeed |
| 14: Multi-seat atomicity | 12, 13 | booking | 37, 113 | Complete multi-seat acceptance and lock ordering checks | Partial conflict rolls back entire booking; reversed selection order avoids deadlocks |
| 15: Expiration | 12–14 | booking | 38–39, 114 | Expiry job and booking-first locking policy | Expired holds released; race-safe with confirmation; repeat execution harmless |
| 16: Mock payment | 12, 15 | payment, payment/gateway | 40–44, 67, 88, 115 | Payment attempts, mock gateway, supported cash recording | Success/failure paths; authorized staff cash; gateway outside critical DB transaction |
| 17: Confirmation | 16 | payment public services coordinating booking/ticket-ready events | 43, 116 | Atomic payment/booking/allocation confirmation; ticket issuance added in 19 | Expired/cancelled rejection, full rollback, expiry race protection, repeated confirmation safe |
| 18: Idempotency | 16, 17 | payment | 44, 117 | Persisted idempotency and unique provider transaction handling | Duplicate and concurrent callbacks have one financial outcome |
| 19: Ticketing | 17, 18 | ticket | 46, 69, 89, 118 | One ticket per passenger, signed QR, issuance in confirmation transaction | No unpaid ticket; duplicate issuance rejected; forged/tampered QR rejected |
| 20: Check-in | 19 | ticket | 46, 89, 119 | Counter verification and check-in | Staff authorization, cancelled/used ticket rejection, repeat/concurrent check-in |
| 21: Full cancellation | 15, 17, 19 | booking, payment/refund public boundary | 47, 120 | Cancellation policy, allocation/ticket invalidation and refund eligibility | Cutoffs, ownership, release inventory, repeat requests and payment-state handling |
| 22: Partial cancellation | 21 | booking/passenger, payment/refund boundary | 48, 121 | Passenger-specific cancellation and booking aggregate status | Only selected allocations/tickets change; last passenger cancels booking |
| 23: Refund | 18, 21, 22 | payment/refund, payment/gateway | 45, 68, 122 | Mock refund workflow linked to payment | Amount bounds, repeat/concurrent refund protection, failure/retry and partial totals |


## Required gates

For every increment: new forward-only Flyway migration if needed; entity/repository, service/policy, DTO/controller and implemented API contract; unit tests plus PostgreSQL integration tests for database behavior; full replacement tests and architecture checks; update current state and handoff. Never alter applied migrations. No empty placeholders or endpoints returning fabricated success.

Increment 12 must already protect writes with sorted TripSeat locks and one atomic transaction. Increments 13/14 expand coverage; they never serve as permission to ship unsafe earlier booking code. Likewise basic duplicate-payment protection belongs with payment writes; Increment 18 completes provider/idempotency hardening before external integration.

Auth and payment transport/API details absent from the source are decisions for their bounded increments, not assumptions in the foundation. No public half-implemented authentication. No gateway chosen before mock flow is stable.

## V1 hardening, then integration

After 23, audit every section 142 requirement as implemented/partial/missing/broken. Finish OpenAPI/Swagger, isolated PostgreSQL test automation, release CI, container/Testcontainers infrastructure, backup/restore and production configuration checks. Docker remains deferred until this separately scoped infrastructure task; daily local development need not change.

Run complete register→login→search→availability→booking→payment→ticket→check-in flow and failure/expiry/duplicate/concurrency/full and partial cancellation/refund scenarios. Produce a release audit with evidence. Only then begin separately requested frontend adaptation using API_COMPATIBILITY and the React blueprint. Data reconciliation and controlled cutover remain separate gates.

Later (24–30 / V1.1/V1.2): real provider, dashboard/reports, business audit, notifications, maintenance and recurring schedules. Source numbering is an order reference, not authorization to include these in V1 now.

## Earlier grouped roadmap cross-reference

| Earlier stage | Specification increment(s) |
|---|---|
| 0 | 0 |
| 1 | 1 (split into bounded tasks) |
| 2, 3, 4 | 2, 3, 4 |
| 5 | 5–6 |
| 6, 7 | 7, 8 |
| 8 | 9–10 |
| 9 | 11 |
| 10 | 12–14 (locking mandatory from 12) |
| 11 | 15 |
| 12 | 16 and 18 |
| 13 | 17 and 19 |
| 14 | 20 |
| 15 | 21–23 |
| 16 | V1 hardening, migration rehearsal and later frontend/cutover tasks |

## Session record

current-task: Objective; relevant specification sections; allowed modules; API/database changes; acceptance tests; exclusions. handoff: completed changes; exact commands/results; failures/unfinished work; configuration/migration notes; exact next bounded task. A successful session does not automatically start the next increment or commit unrelated work.
