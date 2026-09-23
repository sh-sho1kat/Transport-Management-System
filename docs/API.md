> Updated: counter staff, walk-in tickets, cash collection/refunds, and fare updates are documented in [Counter operations](COUNTER-OPERATIONS.md).

> Local testing: import [the Postman collection](../postman/Wayline-Local.postman_collection.json). Backend port is **8088**. See [START-HERE](../START-HERE.md) for accounts and automatic CSRF handling.

# API guide

The checked-in [OpenAPI 3.0 contract](openapi.json) describes all 49 operations, typed request/response schemas, parameters and roles. Generate it with `python3 scripts/openapi.py`; `--check` detects source/contract drift in CI. It is a development artifact, not a public Swagger administration surface.

## Browser authentication

1. GET `/api/v1/auth/csrf`, retain the session cookie and read `{token, headerName}`.
2. POST `/api/v1/auth/login` with `{email,password}`, that cookie, and `X-CSRF-TOKEN: <token>`.
3. Login rotates the session ID and clears the old CSRF token. Fetch `/auth/csrf` again.
4. Send credentials on every request and CSRF on POST/PATCH/DELETE. GET endpoints do not mutate reservations except owned hold-detail reads may expire inventory under a lock.
5. POST `/auth/logout`, then obtain a new CSRF token before the next login.

The React client implements this flow. No access token is stored in local storage. Checkout keeps its temporary hold/key/body in tab-scoped session storage to recover an uncertain confirmation. Logout clears that checkout state. Expired sessions prompt sign-in; mutation requests are never silently replayed.

Public POST authentication/recovery routes still require CSRF. A missing session produces 401; a CSRF failure can produce 403 before authorization is evaluated. CORS preflight rejection is handled by Spring's CORS filter and can be a plain 403 rather than an application error DTO.

## Resource groups

| Prefix | Operations / access |
|---|---|
| `/auth` | register, login, logout, csrf, password-reset-requests, password-resets |
| `/me` | GET profile, PATCH displayName/phone; authenticated |
| `/stops` | GET active stops; public |
| `/trips` | GET paginated search; GET published details and seat map; public |
| `/holds` | POST selection, GET owned hold, DELETE release; passenger |
| `/bookings` | POST confirmation, GET own paginated history; passenger |
| `/bookings/{id}` | GET details/ticket, POST `/cancel`; owner or administrator |
| `/admin/stops`, `/admin/buses`, `/admin/routes` | GET catalog, POST create, PATCH full editable record by ID; administrator |
| `/admin/staff` | GET staff, POST provision, PATCH `/{id}/active`; administrator |
| `/admin/trips` | GET paginated list, POST draft, PATCH draft, POST publish/cancel, PATCH status, GET manifest |
| `/driver/trips` | GET assigned trips, GET `/{id}/manifest`, PATCH `/{id}/status`; assigned driver |
| `/admin/reports/occupancy` | GET paginated trip occupancy and reservation counts |
| `/admin/audit` | GET paginated committed activity, newest first |

Catalog PATCH sends the complete editable DTO, not JSON Patch. Referenced records are archived by `active=false`; no hard-delete endpoint exists. Staff roles are fixed at provisioning. No passenger can request an administrator role.

## Search and pagination

`GET /api/v1/trips?origin=<stop-uuid>&destination=<stop-uuid>&date=2026-10-01&page=0&size=20`

Filters are optional. Origin/destination must match the first/last stops of the route. Date boundaries use `OPERATOR_TIMEZONE`; display timestamps are UTC and the UI formats them in the device timezone. Only published trips with open sales are returned. Pagination is zero-based, size 1–100, with a stable ID tiebreaker.

Paged responses are `{items,page,size,totalItems,totalPages}`. Catalog and driver lists are arrays for the bounded single-operator dataset. Reports show per-trip occupancy and amounts grouped by each trip's currency; they never sum unlike currencies.

## Reserve and confirm

```http
POST /api/v1/holds
Content-Type: application/json
X-CSRF-TOKEN: <current token>

{"tripId":"<uuid>","seatNos":["A1","A2"]}
```

201 returns hold ID, trip ID, seat labels, ACTIVE state, expiry, integer amount, currency and cancellation policy. The server enforces distinct seats, availability, maximum selection, open sales and one active hold per passenger/trip. Client-provided owner, price, status or payment fields are rejected.

```http
POST /api/v1/bookings
Idempotency-Key: 3bf83534-e309-4779-bd55-866efc394ae6
X-CSRF-TOKEN: <current token>
Content-Type: application/json

{"holdId":"<uuid>","contactName":"Demo Passenger",
 "contactEmail":"passenger@example.test","contactPhone":"000-DEMO"}
```

First success: **201**, `Idempotent-Replayed: false`. Same key and identical payload: **200**, `Idempotent-Replayed: true`, same booking ID/current status. Changed payload: **409 IDEMPOTENCY_CONFLICT**. A consumed or expired hold cannot create another booking. Keep the key and exact body after a timeout; retry them, then inspect history before starting over.

A confirmed ticket contains contact and travel snapshots, seats, reference, amount and policy. Online confirmations start with `paymentMethod=PAY_ON_BOARD`, `paymentStatus=UNPAID`. Authorized staff can subsequently record full cash collection. See [Counter operations](COUNTER-OPERATIONS.md) for the payment, refund and fare APIs. No endpoint transfers money.

## Cancellation and lifecycle

POST `/bookings/{id}/cancel` with `{reason}`. Passenger cancellation must be strictly before `departureAt - cancellationHours` and on a published trip; exactly at the cutoff is denied. Administrator override also requires a reason and creates an audit event. Repeated cancellation returns the same cancelled booking.

POST `/admin/trips/{id}/cancel` with `{reason}` atomically cancels a draft/published trip and all reservations. Departed/completed trips cannot be cancelled. PATCH status accepts only PUBLISHED → DEPARTED at/after departure, or DEPARTED → COMPLETED. Drivers may change only assigned trips; administrators may change any eligible trip.

## Errors

```json
{
  "code":"SEATS_UNAVAILABLE",
  "message":"One or more selected seats are unavailable.",
  "fieldErrors":[],
  "traceId":"<uuid>",
  "timestamp":"2026-09-22T10:00:00Z"
}
```

400: malformed/invalid request. 401: sign-in required or revoked. 403: role/CSRF denied. 404: missing or concealed ownership. 405: unsupported method. 409: state/seat/schedule/duplicate/idempotency conflict. 429: request limit. 500: generic unexpected failure with a server-side trace reference.

No stack trace, password, internal SQL or database credentials are returned to the client. Contact field lengths and validation rules are in the generated schemas and request DTOs.
