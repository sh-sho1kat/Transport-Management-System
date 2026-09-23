# Refactor assessment

## Findings and changes

| Before | Consequence | Refactor |
|---|---|---|
| Global controller/service/entity/repository folders | A feature change required searching across the entire application | Feature-owned MVC packages |
| Requests.java, Responses.java, Types.java, Views.java | Unrelated features shared edit hotspots | Separate DTO records, enums and feature mappers |
| BookingService handled holds, counter sales, payments, fares and reservations | Unrelated changes shared transaction-sensitive code | Dedicated use-case services and mandatory transactional inventory helper; fares owned by scheduling |
| Repeated role-name checks in backend and frontend | New roles required many unrelated edits | Explicit capability grants in one backend policy; generated frontend mirror |
| TripService supplied generic paging and AccountService supplied hashing | Features depended on unrelated service implementations | Dependency-neutral shared paging and digest utilities |
| React App mixed navigation, sessions and all page selection | Every page/role affected the shell | Feature registry, session hook, lazy loading and error boundary |
| Large pages defined unrelated editors/dialogs inline | Components were difficult to find or reuse | Feature-owned page/component folders |
| Occupancy loaded full seats/bookings per trip | Memory and database round trips grew with each displayed trip | Bounded aggregate queries and referenced trip entities fetched together |
| One large integration test class | Features shared an editing hotspot | Identity, booking, scheduling and counter suites with test-only fixture |
| Structure had no automated guardrails | Future changes could reintroduce coupling | Architecture and permission tests, UI access tests, generated-policy/API drift checks in CI |

## Compatibility

The refactor preserves existing URLs, HTTP methods/statuses, DTO field shapes, four role names, credentials, ports, CSRF/session behavior, and Flyway V1–V3. No application database migration is needed for this reorganization. TmsApplication remains the IntelliJ entry point. Restart/rebuild the backend after package moves; stale compiled classes must not remain on the classpath.

## Deliberate limits

This remains a single-operator monolith. JPA associations and some read/transaction paths cross features; package ownership is not deployment isolation. No microservices, generic plugin framework, event bus, new payment gateway or arbitrary runtime role editor was introduced. Infrastructure scale and runtime feature isolation require their own measured design work.
