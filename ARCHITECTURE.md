# Layered MVC architecture

The backend uses Spring MVC for the HTTP API. The unchanged React applications supply the UI/view layer. Each resource has its own controller, service interface and implementation, repository interface and Mongo implementation, entity, request DTO, response DTO and mapper.

```text
com.tms
├── TmsApplication.java
├── config/                     CORS, path matching, Mongo startup check, injectable clock
├── controller/                 TimeController, LocationController, TripController, SeatController
├── dto/
│   ├── request/                TimeRequest, LocationRequest, TripRequest,
│   │                          SeatUpdateRequest, BulkSeatUpdateRequest
│   └── response/               Resource DTOs and exact create/update/message wrappers
├── entity/                     Time, Location, Trip, Seat, SeatChanges
├── exception/                  ApiException and global ApiErrors advice
├── mapper/                     Request validation/conversion and entity-to-response mapping
├── repository/                 Typed persistence interfaces
│   └── mongo/                 Mongo adapters, BSON mappers, collection naming
└── service/
    └── impl/                   Resource-specific business logic implementations
```

## Responsibilities

| Layer | Responsibility | Does not do |
|---|---|---|
| Controller | Bind DTOs, select HTTP routes/statuses, delegate to a service interface | Database queries or booking rules |
| Request DTO | Name the accepted request fields; ignore unknown input fields | Represent stored database records |
| Service interface | Define each resource's operations with typed request/response contracts | Expose MongoDB primitives |
| Service implementation | Coordinate validation, booking rules, time, mapping and repository calls | Construct BSON or use MongoTemplate |
| Entity | Represent persisted data with explicit types and Mongo field annotations | Define HTTP response wrappers |
| Mapper | Convert and validate request fields; map entities to stable response DTOs | Execute queries |
| Repository interface | Define typed persistence operations, returning entities and Optional values | Return HTTP responses |
| Mongo repository | Select collections, run atomic queries, map BSON to entities | Apply HTTP validation or business rules |
| Response DTO | Preserve `_id`, `__v`, dates, null fields and original JSON wrappers | Expose entities directly |
| Exception advice | Translate application exceptions and unmatched routes to HTTP responses | Query the database |
| Configuration | Configure cross-cutting HTTP/database/clock behavior | Handle resource operations |

All dependencies use constructor injection. Controllers depend on service interfaces. Services depend on repository interfaces and mappers. Mongo implementations are replaceable behind those interfaces. There are no raw `Map<String,Object>` request/response contracts and no BSON documents in the controller or service packages.

## Request flow

```text
React JSON request
  → Controller + request DTO
  → Service implementation
  → Request mapper / business validation
  → Typed entity or SeatChanges
  → Repository interface
  → Mongo repository + BSON mapper
  → Existing MongoDB collection

Persisted entity
  → Response mapper
  → Response DTO
  → Controller JSON response
```

For example, `POST /api/admin/create-trip` goes through `TripController`, `TripService`, `TripServiceImpl`, `TripMapper`, `TripRepository` and `MongoTripRepository`. `TripDocumentMapper` handles BSON storage; `TripMapper` handles the request and response boundary. `CreateTripResponse` retains `{message, newTrip}`.

## Compatibility choices

- Request DTOs use named `JsonNode` fields at the inbound boundary. This is intentional: the old API accepts some non-string scalar values, treats false/zero/empty values as missing, and returns distinct 400/500 errors. Immediately binding everything to Java strings or adding blanket `@NotBlank` validation would change those contracts. Mappers validate and convert these fields into typed entities before any repository call. Response DTOs are fully typed.
- Fixed-collection entities have Spring Data `@Document` and field annotations. The repository adapters explicitly map BSON to prevent `_class` fields or version increments from changing the existing data format. `__v` is legacy metadata, not a new optimistic-locking `@Version` field.
- Repository interfaces are application-owned persistence contracts rather than generated `MongoRepository` interfaces. Their MongoTemplate-backed implementations preserve atomic `$set` updates and support the original dynamic seat collections. Services are independent of that persistence choice.
- `SeatChanges` explicitly distinguishes an omitted status from a provided null and distinguishes a status-only update from clearing/replacing booking details. This avoids read-modify-save races and unintended changes to existing student fields.
- `Clock` is injected, allowing booking timestamps to be tested deterministically without a running database.
- The original nontransactional bulk behavior is preserved. No `@Transactional` promise is added where the old API did not provide one.
- The legacy unused user/email modules remain outside the active API. The frontend source files are unchanged.

## Tests

`ApiCompatibilityTest` covers the public API and Mongo persistence. `CollectionNamesTest` checks existing Mongoose collection naming. `ServiceIsolationTest` tests business validation, injected time, and omitted/null patch semantics with mocked repository interfaces and no Spring context or database. `NodeParityTest` compares live HTTP responses with the original Node server.

Run `./mvnw verify` from `backend/`. See `VALIDATION.md` for the optional Node comparison command and recorded results.
