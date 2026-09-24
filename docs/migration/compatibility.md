# Compatibility boundary

| Concern | Current backend | Replacement target |
|---|---|---|
| Port | 8088 | 8089 during migration |
| Database | wayline on 55432 | wayline_next on 55433 |
| API | /api/v1 | /api/v1 for business endpoints |
| Authentication | Session cookie + CSRF | JWT + persisted refresh lifecycle (Increment 1) |
| IDs | UUID | BIGINT with explicit mapping |
| Money | Integer minor units | Decimal money |
| Roles | PASSENGER, ADMIN, DRIVER, COUNTER_STAFF | PASSENGER, ADMIN, MANAGER, COUNTER_STAFF |
| Inventory | Existing hold/booking model | Segment allocations + pending bookings |
| Payment | Existing cash tracking semantics | Payment-before-confirmation, attempts and refunds |

Existing frontend API calls, CSRF handling, credentials and CORS configuration are untouched. It remains pointed at the current backend. The replacement is not a drop-in URL switch; frontend adaptation is a separate gate after V1 hardening.

Do not route real checkout traffic to both services for a single departure. No application credentials or existing session cookies are migrated implicitly. Increment 0 denies cross-origin access; define replacement CORS and refresh-token/CSRF transport together before integration.

Both backends intentionally share the /api/v1 prefix on different ports. This does not make their contracts compatible. See [the API compatibility matrix](../API_COMPATIBILITY.md) and [ADR 0002](../decisions/0002-final-document-alignment.md).
