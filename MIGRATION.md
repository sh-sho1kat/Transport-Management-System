# Migration and compatibility notes

## Scope of this release

The earlier version converted Node.js to Spring Boot while preserving MongoDB and the two original React applications. This version follows the subsequent request for a general bus platform with role-based handling and PostgreSQL. It replaces the legacy frontend/API together. It is **not** a drop-in replacement for an old client still calling `/api/admin`.

The earlier sources remain in the workspace's `archive/mongodb-version/`; they are excluded from the new deployment and release ZIP. `sources/` and the supplied original ZIP were not modified. No production MongoDB database was contacted, modified or imported.

## Old data cannot safely become authenticated bookings automatically

Original trips contain `busID`, `tripID`, `startlocation`, `destination`, `date`, and a `departuretime` string. Seats live in dynamic Mongoose collections, with `seatNo`, `bookingStatus`, `studentId`, `studentMail`, `bookingDate` and `bookingTime`.

Missing evidence includes authenticated passenger identities, arrival times, authoritative bus identities/layout coordinates, assigned drivers, exact timezone interpretation, historical fares/currency and payment facts. A student identifier is not a valid account foreign key. The new required foreign keys and snapshots deliberately prevent inventing those facts.

| Legacy data | Reviewed target |
|---|---|
| Location strings | Stops plus ordered route-stop relationships |
| Global time strings | Explicit timezone-reviewed departure/arrival instants on trips |
| Bus text IDs | Verified physical buses with unique registration/layout |
| Trip ID / Mongo ObjectId | UUID trip plus retained external-ID mapping |
| Dynamic seat collections | Fixed trip_seats inventory rows |
| Booked seat/student fields | Unclaimed historical evidence; attach only after a verified account claim/review |
| Unauthenticated users | Reference evidence; require new authenticated account enrollment |

## Offline dry run

The shipped review tool reads a JSON export and writes a private report directory; it has no database connection or import mode.

```bash
python3 scripts/legacy-dry-run.py docs/legacy-export.example.json --output /tmp/wayline-review
```

For your export, assemble `trips`, `seatCollections`, `users`, `locations`, and `times` as in the fictional example. Map each seat collection to its exact original `tripID`; obtain real collection names from the source database rather than guessing Mongoose pluralization. Preserve Mongo Extended JSON IDs/dates.

Outputs:

- `summary.json`: source hash and reconciliation counts, with zero active records imported.
- `id-mapping.json`: stable proposed UUID mapping for trips and inventory.
- `review-plan.json`: original source evidence, candidate inventory, unclaimed booking markers and missing required fields.
- `exceptions.json`: duplicate/missing identifiers, invalid seats, unmatched collections and records needing review.
- `legacy-reference.json`: preserved user/location/time evidence.

The directory is mode 0700 and files 0600; treat it as sensitive when using real records. Output must be a new directory. Review output is not an executable import. It intentionally does not create password hashes, fares, payment status, passenger consent or multi-seat grouping from timestamps. Source historical booking state remains explicit in the report.

## Reviewed cutover

1. Back up MongoDB and the target database. Stop legacy writes; record export time, collection counts and checksums.
2. Run the dry-run tool and resolve all mapping exceptions with the operator.
3. Create verified fleet, staff, stops and routes in a staging instance; preserve mappings.
4. Keep historical unclaimed bookings outside active sales until a separate reviewed historical-import/claim process is implemented. Current booking tables require known account/fare/time facts, so this release does not force incomplete historical records into them.
5. Reconcile source seat counts, historical occupied seats and identities; review proposed future schedules and publish only valid ones.
6. Deploy the new UI and secured `/api/v1` backend together; validate role access, recovery, seat allocation and tickets on staging before public traffic.
7. Retain the source export and mapping securely. After new writes, any rollback must reconcile new bookings and cancellations rather than restoring an old snapshot alone.

A production historical importer/verified claim UI remains separate work because no real export or reviewed mappings were supplied. The new application can start with a clean PostgreSQL database now.
