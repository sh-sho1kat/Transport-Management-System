# Verification results

Verified on 2026-09-22 using the supplied `Transport_Management_System-main.zip` as the source reference.

## Results

- **250 frontend files** matched the original ZIP byte-for-byte. SHA-256 values are recorded in `frontend-sha256.json`. Vendored `node_modules` are excluded; no source or lockfile was changed.
- **31 automated tests passed**: 19 API/integration cases and 12 collection-naming cases.
- **70 real HTTP comparisons passed** against the original Node server, grouped in an additional differential test. Both servers used isolated databases served by `mongo-java-server`.
- All **22 mounted endpoint definitions** are exercised by the tests.
- The final Maven `verify` run produced the executable Spring Boot JAR.

The Java build used the installed JDK 25 with `--release 21`. Spring Boot is pinned to 3.5.16. The existing Java 21 runtime on the migration machine did not contain `javac`, so it was not used as a compiler. Maven 3.9.11 was used; the included wrapper pins the same version.

## Coverage

The contract checks include create/read/update/delete for times, locations and trips; field validation; BSON ObjectId and UTC date serialization; readback of existing-shaped Mongo documents; malformed ObjectIds; missing records; unknown field filtering; 40-seat initialization and reset; sorting; single and bulk bookings; cancellation and null clearing; student and booked-seat queries; missing bulk seats; nontransactional partial writes; original enum-update behavior; CORS/preflight; unauthenticated access; HEAD; trailing slashes; case-insensitive routes; unsupported methods; inactive routes; and the original shadowed bus lookup.

Direct HTTP comparisons check status codes, application JSON values and CORS headers. Independently generated `_id` values and server-generated booking timestamps are normalized after checking their format. Trip dates, messages, keys, nulls, arrays, sorting and scalar types are compared directly. This confirms request/response compatibility for the tested cases, not identity of independently generated IDs or timestamps.

## Reproduce

From `backend/`:

```sh
./mvnw verify
```

This runs the 31 standalone tests and skips the optional Node differential test. Tests start their own in-memory Mongo wire-protocol server. They do not use the database in your `.env`.

To also compare with the retained original Node source, install its dependencies first:

```sh
cd backend-node-reference
npm ci
cd ../backend
./mvnw -Dnode.reference="$(cd ../backend-node-reference && pwd)" verify
```

Use an absolute original-backend path for `node.reference` on Windows. Node must be available on PATH. The differential test overrides its `PORT`, `MONGO_URL`, timezone and locale, starts it as a child process, and shuts it down afterward. The retained Node source has no `.env`. Never use a modified reference server that ignores these isolation settings.

Test reports are generated under `backend/target/surefire-reports/`. The Node subprocess log is `backend/target/node-parity.log`.

## Limits

No production MongoDB deployment, browser UI session, SMTP account, or production data was accessed. Frontend builds were not rerun because the frontend files are unchanged. Tests use a MongoDB protocol emulator and do not validate deployment-specific TLS/authentication, replica-set transactions/failover, or load/concurrency. Parser/transport and legacy behavior boundaries are recorded in `MIGRATION.md`.
