# Compatibility boundary

| Concern | Current backend | Replacement target |
|---|---|---|
| Port | 8088 | 8089 during migration |
| Database | wayline on 55432 | wayline_next on 55433 |
| API | /api/v1 | /api/v2 for business endpoints |
| Authentication | Session cookie + CSRF | JWT + persisted refresh lifecycle (Stage 1) |
| IDs | UUID | BIGINT with explicit mapping |
| Money | Integer minor units | Decimal money |
| Roles | PASSENGER, ADMIN, DRIVER, COUNTER_STAFF | PASSENGER, ADMIN, MANAGER, COUNTER_STAFF |
| Inventory | Existing hold/booking model | Segment allocations + pending bookings |
| Payment | Existing cash tracking semantics | Payment-before-confirmation, attempts and refunds |

Existing frontend API calls, CSRF handling, credentials and CORS configuration are untouched. It remains pointed at the current backend. The replacement is not a drop-in URL switch; frontend adaptation is an explicit Stage 16 gate.

Do not route real checkout traffic to both services for a single departure. No application credentials or existing session cookies are migrated implicitly. Stage 0 denies cross-origin access; define v2 CORS and refresh-token/CSRF transport together before integration.
