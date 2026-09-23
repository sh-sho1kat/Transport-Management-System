# Implementation roadmap

Only Stage 0 is included in this change. Larger milestones must be split into multiple sessions. Do not implement the whole roadmap in one request.

| Stage | Deliverable | Status |
|---|---|---|
| 0 | Foundation, profiles, Flyway, safe errors, health and boundary checks | Implemented; see handoff for validation |
| 1 | Users/roles and registration; JWT and refresh lifecycle; authorization (split into bounded slices) | Not started |
| 2 | Stations | Not started |
| 3 | Routes and ordered stops | Not started |
| 4 | Seat layouts and seats | Not started |
| 5 | Buses and independent drivers | Not started |
| 6 | Trips, assignments and scheduling conflicts | Not started |
| 7 | TripSeat snapshots | Not started |
| 8 | Segment fares and search | Not started |
| 9 | Overlap policy and availability | Not started |
| 10 | Pending bookings, passengers and atomic locked allocations | Not started |
| 11 | Expiration and confirmation race protection | Not started |
| 12 | Mock/cash payments and idempotency | Not started |
| 13 | Atomic confirmation, tickets and signed QR | Not started |
| 14 | Counter verification and check-in | Not started |
| 15 | Full/partial cancellation and mock refunds | Not started |
| 16 | End-to-end testing, migration rehearsal and frontend cutover preparation | Not started |
| Later | Reports, audit, notifications, maintenance, recurring schedules, real gateway | Not started |


Every completed feature needs migration, API documentation and focused tests. Inventory locking and meaningful PostgreSQL concurrency tests must ship in Stage 10, not after writable bookings. Stage 11 expands expiry/confirmation coordination. No production release before Stage 16 and the separate deployment gates.
