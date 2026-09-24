# Current-to-target migration map

This is a design inventory, not an executable importer. Evidence: backend/src/main/resources/db/migration/V1__relational_schema.sql, V2__relationship_indexes.sql, V3__counter_sales_payments.sql and feature entities/DTOs under backend/src/main/java/com/tms. Target: FINAL_SPEC sections 13–74.

| Current table/concept | Target owner/model | Conversion / missing information |
|---|---|---|
| accounts | user: users, user_roles | UUID→BIGINT ledger; display_name→reviewed first/last names (do not invent a split); active→reviewed status; one role→role rows; BCrypt compatibility review; verified-email history may be unavailable |
| DRIVER account, trips.driver_id | fleet/driver: Driver; trip/driver: TripDriverAssignment | Independent driver data, employee/license details may be missing; no automatic staff-role promotion; preserve legacy identity/archive link |
| reset_tokens / active sessions | No direct refresh-token mapping | Password-reset tokens are not refresh tokens; do not migrate sessions as JWT refresh credentials; new login required |
| stops | station: Station | Existing name/city/address/active; target station code and location details need reviewed source/enrichment |
| routes | route: Route | Add reviewed origin/destination refs, distance/duration metadata; first/last ordered stop must match endpoints |
| route_stops | route: RouteStop | Zero-based sequence→positive stopOrder; map station IDs; offsets, distances and boarding/dropping eligibility need validation |
| buses + bus_seats | fleet/bus: Bus; fleet/seatlayout: SeatLayout, Seat | Existing per-bus layout→explicit reviewed layout; registration→registrationNumber; status/coach/seat type/deck metadata not inferred blindly |
| trips | trip: Trip | UUID→BIGINT; review DRAFT/PUBLISHED vs SCHEDULED/BOARDING transitions; driver assignment separate; preserve departure/timezone meaning and scheduling constraints |
| trips.fare_minor/currency | trip/fare: TripFare | One whole-trip fare cannot invent every segment/seat-type fare; exact currency-aware decimal conversion and explicit pricing review |
| trip_seats | trip/seat: TripSeat | Preserve historical layout snapshot; separate occupancy from seat definition; availability becomes segment-dependent |
| holds + hold_seats | booking: pending Booking, BookingPassenger, SeatAllocation | No new Hold entity; expire stale holds under approved import rules; missing passenger/segment data must be reviewed or archived |
| bookings + booking_seats | booking: Booking, BookingPassenger, SeatAllocation | Contact fields do not prove per-passenger details; current confirmed-unpaid records conflict with payment-before-confirmation and need explicit treatment |
| bookings.sales_channel/sold_by_id/passenger_id | Booking creator and optional customer | Counter rows retain known actor; current online actor may be customer; never create a fabricated user to fill unknown creator |
| bookings.payment_status/payment_method/payment_updated_* | payment: Payment, Refund | Cash status is not a payment-attempt ledger; PAID/REFUNDED flags lack full histories; retain proven facts/archive, no invented attempts or callbacks |
| idempotency_records | Booking request / payment idempotency policies | Current booking-request keys are not provider callback identifiers; retain source operation meaning |
| Printable booking view / reference | ticket: Ticket per BookingPassenger | No target Ticket table or signed QR in legacy; do not issue valid target tickets for unverified confirmations |
| audit_events | Later audit: AuditLog or archive | Preserve actor/action/resource/reason facts; retain UUID mapping and distinguish legacy from new event semantics |
| Occupancy and booking-value reports | Later reporting projections | Current booked value is not paid revenue; reconcile collected/refunded totals separately |
| No counterpart | RefreshToken, partial cancellation, provider payments, notifications, maintenance/templates | Implement in assigned increments; no fictitious historical rows |

## Data safety

Use a durable source UUID→target BIGINT mapping ledger for every imported PK/FK. Convert money with exact decimals and documented currency exponent; reject overflow of NUMERIC(12,2). Preserve instants with TIMESTAMPTZ. Keep counts and money totals for imported, rejected and archived records.

Neither old V1–V3 nor new V1__foundation.sql may be edited. Target migrations start V2. Migration rehearsal uses an isolated database; never two writable sellers for a live departure. See [data mapping/reconciliation](migration/data-mapping.md) and [cutover](migration/cutover.md).
