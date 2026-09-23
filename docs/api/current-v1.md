# Current frontend API contract

The authoritative existing machine-readable contract is [../openapi.json](../openapi.json). Preserve its URLs, methods, request/response shapes, errors, status codes, cookies, CSRF and CORS behavior. Frontend source remains unchanged.

Current base URL: http://localhost:8088. Authentication uses sessions and CSRF, not replacement JWT. Current IDs are UUIDs and monetary amounts are integer minor units. Use existing API/operations docs for exact behavior.

Snapshot of current operations (generated from the existing contract):

| Method | Path |
|---|---|
| POST | `/api/v1/holds` |
| GET | `/api/v1/holds/{id}` |
| DELETE | `/api/v1/holds/{id}` |
| POST | `/api/v1/bookings` |
| GET | `/api/v1/bookings` |
| GET | `/api/v1/bookings/{id}` |
| GET | `/api/v1/bookings/{id}/ticket` |
| POST | `/api/v1/bookings/{id}/cancel` |
| POST | `/api/v1/counter/bookings` |
| PATCH | `/api/v1/bookings/{id}/payment` |
| GET | `/api/v1/stops` |
| GET | `/api/v1/admin/stops` |
| POST | `/api/v1/admin/stops` |
| PATCH | `/api/v1/admin/stops/{id}` |
| GET | `/api/v1/admin/buses` |
| POST | `/api/v1/admin/buses` |
| PATCH | `/api/v1/admin/buses/{id}` |
| GET | `/api/v1/admin/routes` |
| POST | `/api/v1/admin/routes` |
| PATCH | `/api/v1/admin/routes/{id}` |
| GET | `/api/v1/auth/csrf` |
| POST | `/api/v1/auth/register` |
| POST | `/api/v1/auth/login` |
| POST | `/api/v1/auth/logout` |
| GET | `/api/v1/me` |
| PATCH | `/api/v1/me` |
| POST | `/api/v1/auth/password-reset-requests` |
| POST | `/api/v1/auth/password-resets` |
| GET | `/api/v1/admin/staff` |
| POST | `/api/v1/admin/staff` |
| PATCH | `/api/v1/admin/staff/{id}/active` |
| GET | `/api/v1/admin/reports/occupancy` |
| GET | `/api/v1/admin/audit` |
| GET | `/api/v1/trips` |
| GET | `/api/v1/trips/{id}` |
| GET | `/api/v1/trips/{id}/seats` |
| GET | `/api/v1/admin/trips` |
| POST | `/api/v1/admin/trips` |
| GET | `/api/v1/counter/trips` |
| PATCH | `/api/v1/admin/trips/{id}` |
| POST | `/api/v1/admin/trips/{id}/publish` |
| POST | `/api/v1/admin/trips/{id}/cancel` |
| PATCH | `/api/v1/admin/trips/{id}/status` |
| PATCH | `/api/v1/driver/trips/{id}/status` |
| GET | `/api/v1/driver/trips` |
| GET | `/api/v1/admin/trips/{id}/manifest` |
| GET | `/api/v1/driver/trips/{id}/manifest` |
| GET | `/api/v1/counter/trips/{id}/manifest` |
| PATCH | `/api/v1/admin/trips/{id}/fare` |


Run `python3 scripts/openapi.py --check` to check the existing contract. Refresh this inventory when an authorized current-backend change occurs. The snapshot is a navigation aid, not a replacement schema.
