# Migration guide

## Scope and structure

All **22 endpoints mounted by the original `backend/index.js`** are implemented in Spring Boot. The two frontends continue to call `http://localhost:8000/api/admin`. No frontend source, service, dependency manifest, lockfile, or API URL was edited.

The source was inspected across the bootstrap, six route files, six controllers, five model files, environment variable names, all eight frontend service modules, trip creation, seat selection/booking, and authentication screens. Source comments and the original README were treated as reference; the mounted routes and executable code determine compatibility.

The migration uses separate controller, DTO, entity, mapper, repository, service, exception and configuration packages. Each resource has a typed service and repository interface with a dedicated implementation. MongoDB remains the database; BSON and MongoTemplate access are confined to the Mongo repository adapters. See [ARCHITECTURE.md](ARCHITECTURE.md) for the complete structure and request flow. Explicit persistence mapping avoids Java-specific `_class` fields, renamed properties, pagination wrappers, or different ID encodings.

## Configuration and running

Use a JDK 21 or newer. The project targets Java 21 and pins Spring Boot 3.5.16. The included Maven wrapper selects Maven 3.9.11 and downloads it on first use. Its Java compatibility baseline is documented by [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

From `backend/`, copy `.env.example` to `.env` and set:

| Setting | Default | Meaning |
|---|---|---|
| `PORT` | `8000` | Must remain 8000 for the supplied frontend URLs. The original `.env` also selected 8000, overriding Node's fallback of 5000. |
| `MONGO_URL` | `mongodb://localhost:27017/tms` | Use the exact URI and database used by the existing application to retain its data. Include the database name explicitly. |
| `BOOKING_TIME_ZONE` | `Asia/Dhaka` | Zone used for human-readable booking time. Set it to the old Node server's timezone if different. |
| `BOOKING_TIME_LOCALE` | `en-US` | Locale used for human-readable booking time. Match the old Node server's locale if different. |

The local `.env` is read as a Spring properties file from the process working directory: use `NAME=value` with **no shell quotes and no `export` prefix**. Environment variables override this file. Run from `backend/` or configure your IDE's working directory accordingly. Do not copy credentials into Java source. The archived `.env` was not included in the delivered project.

```sh
./mvnw spring-boot:run
# Or build and run:
./mvnw clean verify
java -jar target/transport-backend-1.0.0.jar
```

Use `mvnw.cmd` on Windows. MongoDB must be reachable and the application credentials must permit the same reads/writes and dynamic collection creation as before. Startup performs a database ping and fails if the database cannot be reached. A running frontend alone does not start MongoDB or the Java backend.

Example read-only check:

```sh
curl http://localhost:8000/api/admin/get-trip
```

For local frontend development, use the existing `npm ci` and `npm run dev` scripts in each frontend directory. Choose separate frontend ports (for example, 5173 and 5174). CORS permits both. No frontend rebuild or source edit is required specifically for this backend replacement.

## Preserved API contracts

All paths below are relative to `/api/admin`. Each created document contains a string `_id` and numeric `__v: 0`. Responses retain original casing, capitalization, messages, spelling and wrappers. Date values are stored as BSON dates and emitted as ISO UTC strings with milliseconds. Null student and booking fields remain explicit nulls.

| Method | Path | Success response |
|---|---|---|
| POST | `/create-time` | 201: `{message, newTime}` |
| GET | `/get-time` | 200: time document array |
| GET | `/get-time/{id}` | 200: time document |
| PUT | `/update-time/{id}` | 200: `{message, updatedTime}` |
| DELETE | `/delete-time/{id}` | 200: `{message}` |
| POST | `/create-location` | 201: `{message, newLocation}` |
| GET | `/get-location` | 200: location document array |
| GET | `/get-location/{id}` | 200: location document |
| PUT | `/update-location/{id}` | 200: `{message, updatedLocation}` |
| DELETE | `/delete-location/{id}` | 200: `{message}` |
| POST | `/create-trip` | 201: `{message, newTrip}` |
| GET | `/get-trip` | 200: trip document array |
| GET | `/get-trip/{id}` | 200: trip document |
| PUT | `/update-trip/{id}` | 200: `{message, updatedTrip}` |
| DELETE | `/delete-trip/{id}` | 200: `{message}` |
| POST | `/seats/create/{tripId}` | 201: `{message, tripId}`; replaces seats with 01–40 |
| GET | `/seats/{tripId}` | 200: seat array sorted by `seatNo` |
| GET | `/seats/{tripId}/booked` | 200: booked seat array sorted by `seatNo` |
| GET | `/seats/{tripId}/student/{studentId}` | 200: matching seat array sorted by `seatNo` |
| GET | `/seats/{tripId}/{seatNo}` | 200: seat document |
| PUT | `/seats/{tripId}/{seatNo}` | 200: `{message, seat}` |
| PUT | `/seats/{tripId}` | 200: `{message, seats, tripId}` |

Request bodies:

```json
{"time":"08:00"}
```

```json
{"location":"Campus"}
```

```json
{
  "busID":"BUS01",
  "tripID":"TRIP12345",
  "startlocation":"Campus",
  "destination":"City",
  "date":"2026-09-21T08:00:00.000Z",
  "departuretime":"08:00"
}
```

```json
{"bookingStatus":"booked","studentId":"12345","studentMail":"student@example.test"}
```

```json
{"seats":[{"seatNo":"01","bookingStatus":"booked","studentId":"12345","studentMail":"student@example.test"}]}
```

A cancellation sends `bookingStatus: "unbooked"`; the server clears `studentId`, `studentMail`, `bookingDate` and `bookingTime`. Client-supplied booking timestamps are ignored, just as in Node. Bulk updates share a server timestamp and return results in request order. A missing seat in bulk results is `null`.

### Validation and errors

- Time/location create and update require a truthy `time`/`location`. Missing input is 400 with the original message.
- Trip create and update require all six fields. Partial updates still return 400 `{"message":"All fields are required"}`.
- Existing Mongoose scalar-to-string conversion is reproduced for strings, booleans and numbers. Unknown create/update fields are ignored. ISO instants, offsets, date-only strings and numeric epoch milliseconds are supported for trip dates.
- A missing valid ObjectId returns the original 404 message. An invalid ObjectId or invalid date reaches the original 500 `{"message":"Server error"}` behavior.
- Empty list reads return `[]` with 200. Student queries with no results return 404 `{"message":"No bookings found for this student in this trip"}`.
- Single-seat booking requires student details and returns 400 when missing. Bulk booking preserves the original 500 `{"error":"Student details required for seat 01"}` form.
- Bulk operations remain nontransactional. Valid entries can persist even if another entry fails. There is no new rollback or all-or-nothing guarantee.
- The original `findOneAndUpdate` calls do not enable Mongoose enum validation. The migration retains that behavior rather than rejecting previously accepted custom statuses.
- Controller database failures keep their original `message` versus `error` key. Unmounted routes and unsupported methods return Express-style HTML 404 responses. Case-insensitive routes, trailing slashes, HEAD and OPTIONS are supported.

### Authentication, CORS, uploads, and email

The original mounted API has **no authentication or authorization middleware**, tokens, sessions, password hashing, roles, or protected routes. The admin sign-in/sign-up pages are templates without backend authentication calls. The replacement preserves public access; it does not silently add a login requirement.

CORS mirrors `cors()` defaults: wildcard origin, `GET,HEAD,PUT,PATCH,POST,DELETE`, reflected preflight request headers, no credential allowance, and 204 preflight responses.

There are no mounted file-upload routes or upload storage integrations. Multipart auto-processing is disabled. Frontend image-picker controls do not upload to this backend.

The `bookingRoute.js` and `EmailPdfService.js` files are never imported by `index.js`; the frontend never calls their confirmation route. Their SMTP/PDF code also contains unresolved runtime configuration issues. Consequently **neither the original active backend nor this replacement sends booking emails or creates PDFs**, despite the frontend's existing success text. The unused files remain in `backend-node-reference/` for reference; no SMTP settings are needed for the migrated API. Activating email delivery is separate feature work, not an existing behavior reproduced by this migration.

## Existing database: no schema migration required

| Data | MongoDB collection | Existing fields |
|---|---|---|
| Times | `timetables` | `_id`, `time`, `__v` |
| Locations | `locationtables` | `_id`, `location`, `__v` |
| Trips | `addtrips` | `_id`, `busID`, `tripID`, `startlocation`, `destination`, `date`, `departuretime`, `__v` |
| Seats | Dynamic Mongoose collection name for `tripID` | `_id`, `seatNo`, `bookingStatus`, `studentId`, `studentMail`, `bookingDate`, `bookingTime`, `__v` |
| Unmounted user module | `users` | Untouched; no active API was mounted in Node |

Seat collection names use the original Mongoose 8 pluralization rules, including uncountable nouns and numeric suffixes. Examples: `TRIP12345` → `trip12345`, `TRIP` → `trips`, `City` → `cities`. Do **not** rename existing collections or consolidate seats into a new table. Mongo `_id` values remain BSON ObjectIds; JSON encoding alone turns them into strings. No SQL/Flyway/Liquibase migration is introduced. No new uniqueness indexes or foreign-key constraints are added.

Cutover:

1. Take your normal database backup and record the old URI/database name.
2. Stop Node before allowing Java to write to the same database.
3. Configure Java's `MONGO_URL` to the same database and set the server locale/timezone to match the old installation.
4. Start Java on port 8000. Read existing trips and seats through the original frontends.
5. Exercise a new disposable trip for create/book/cancel checks. Do not initialize seats for an existing booked trip: that operation intentionally deletes its seats first.

Rollback: stop Java and restart the original Node backend with the same database settings. The on-disk document layout is unchanged. Normal business data written after cutover remains; restore a backup only if you specifically need to undo those business writes.

## Component mapping

| Node.js module or behavior | Spring Boot component |
|---|---|
| `index.js`, Express bootstrap | `TmsApplication`, `config/DatabaseStartupCheck`, `application.properties` |
| Time/location/trip route modules | Resource-specific classes in `controller/` |
| Time/location/trip controllers | `service/TimeService`, `LocationService`, `TripService` and their `service/impl/` implementations |
| Mongoose time/location/trip models | Typed records in `entity/`, request/response DTOs, API mappers and BSON mappers |
| `seatBookRoute.js`, `seatBookController.js` | `controller/SeatController`, `service/SeatService`, `service/impl/SeatServiceImpl` |
| `seatBookModel.js` and model factory | `entity/Seat`, `entity/SeatChanges`, `repository/SeatRepository`, `repository/mongo/MongoSeatRepository`, `MongooseCollectionNames` |
| Mongoose queries | Typed repository interfaces and MongoTemplate-backed implementations in `repository/mongo/` |
| Request casting and JSON serialization | `dto/request/`, `dto/response/`, `mapper/` |
| `cors()` and Express path behavior | `config/ExpressCompatibilityFilter`, `config/WebConfiguration` |
| Controller catch blocks | `exception/ApiException`, `exception/ApiErrors`, `service/impl/ServiceOperation` |
| Server booking timestamps | Injectable `Clock` configured in `config/ClockConfiguration` |
| `dotenv` | Environment variables or local `.env` imported as Spring properties |
| Commented-out user mount, unused mail route, unmounted `deleteAllSeats` | Remain unavailable; original source retained for reference |
| Frontend API services | Unchanged; same HTTP contracts |

## Preserved limitations and compatibility boundaries

- The two original `/get-trip/:id` and `/get-trip/:busID` declarations are the same Express route shape. The first always wins. The unused frontend `getTripsByBusId` helper therefore never had a working bus lookup. Java preserves ID lookup, including 500 for non-ObjectId bus strings. The actual screens fetch all trips and filter client-side, so no route fix was necessary.
- Seat updates are last-writer-wins and do not prevent concurrent double booking. Seat reset deletes prior bookings. Deleting a trip does not delete its seat collection. These are existing behaviors retained for compatibility.
- No production database or SMTP account was accessed during migration. Tests use an isolated Mongo wire-protocol emulator, not a production MongoDB cluster. Deployment-specific TLS, Atlas access rules, failover and concurrent load still need validation in your environment.
- Compatibility tests cover frontend JSON requests and controller outcomes. Invalid JSON/parser errors, non-JSON request bodies, compression, body-size limits and exotic JavaScript date coercions are not byte-for-byte Express emulation: Spring/Tomcat supplies its own parser/transport handling. No frontend call relies on these cases. Framework-generated errors can therefore differ from Express development stack-trace pages.
- Bulk duplicate updates execute sequentially in request order; Node dispatched them concurrently. No specific winner was guaranteed for duplicate seat numbers in the same original request. Frontend requests contain distinct seat numbers.
- Java locale formatting can differ for locales outside the tested `en-US` setting. BSON timestamps and ISO UTC JSON dates are unaffected.
