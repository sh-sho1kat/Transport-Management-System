# Data mapping and reconciliation plan

No importer or data movement is implemented at Stage 0. `scripts/migration/inventory.py` reports local contract/migration filenames only; it does not connect to either database.

| Source | Target decision |
|---|---|
| UUID primary and foreign keys | Persistent mapping ledger `(source table, source UUID, target table, target BIGINT)`; unique source/target entries, deterministic resumable import |
| Minor-unit money | Convert with the source currency exponent using exact decimals; verify limits and totals, never float arithmetic or a blanket division for every currency |
| Accounts/password hashes | Review BCrypt compatibility; preserve eligible identities, require fresh login, never migrate active sessions as refresh tokens |
| DRIVER login users | Map reviewed operational details to independent Driver records; retain original identity in archive if no valid new account role applies |
| Staff roles | Explicit approved role/permission mapping; never promote staff automatically |
| Stops/routes | Map to Station/Route/RouteStop, validate ordered chain and endpoints before trip import |
| Buses/layouts | Map layouts and seats, preserve historical TripSeat snapshots |
| Holds/bookings | Import only when status and segment/passenger data are complete; expire obsolete holds under an approved rule |
| Cash/payment history | Preserve proven financial facts; do not fabricate provider attempts or paid status |
| Passenger details missing | Manual review or archive; do not invent names/documents to satisfy mandatory fields |
| Tickets | Preserve old tickets for archive lookup; issue replacement tickets only under verified target confirmation rules |

Before any production run: immutable source backup, dry run into a fresh replacement database, reject/quarantine report, mapping ledger, record counts, currency totals, payment/refund reconciliation, per-departure inventory overlap checks, sample ticket verification, signed review. Document the actual export/import format and checkpoint/retry semantics before implementing the importer.

Historical records that cannot be represented truthfully stay in a read-only legacy archive with a documented authorized lookup path. Account for them separately in reconciliation rather than dropping them.
