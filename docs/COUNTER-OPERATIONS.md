# Counter sales and cash payments

Run the backend from IntelliJ again after this update. Flyway V3 upgrades the existing database without removing reservations. Restart the frontend if necessary. Ports and database credentials remain unchanged.

## Local sign-in

Counter staff: `counter.demo@example.test` / `DemoPass123!` (created only if missing, under the default local profile). Existing accounts retain their passwords.
Administrators can create more counter staff through **Team & roles → Add staff member → Counter staff**.

## Workflow

1. Counter staff open **Ticket counter**; administrators use **Trip operations**.
2. Choose a published trip and **Sell walk-in ticket**. Select up to four available seats, enter name and phone; email is optional. No passenger account is created.
3. Choose **Unpaid** or **Paid — cash received at counter**. Issue and print the ticket. Counter sales remain open until departure; the earlier online sales cutoff still applies to passenger bookings.
4. Open **Manifest** to find both online and counter bookings. Choose **Update payment**, receive the full cash amount, and enter a collection note. Assigned drivers have the same collection control for their published/departed trips.
5. Administrators may correct a mistakenly paid reservation back to unpaid with a reason. Counter staff/drivers cannot reverse collections.
6. Cancelling a paid booking sets **REFUND_DUE**. After returning cash, counter staff/admin select **Update payment** to record **REFUNDED**. No money is transferred by this application. Counter cancellation follows the ordinary cancellation deadline; admin override remains available.
7. Administrator **Update fare** changes the per-seat fare for future sales before departure. Active holds and existing tickets retain their price. A counter request using an outdated total returns 409; close/reopen the sale after refreshing trips.

Only full cash collection/refund is supported. There are no partial payments or electronic transactions. A completed trip's collections can be reconciled by counter staff/admin, not drivers.

## Roles

| Action | Passenger | Counter staff | Assigned driver | Admin |
|---|---|---|---|---|
| Online self-service booking | Yes | No | No | No |
| Walk-in sale and ticket | No | Yes | No | Yes |
| Read manifests | No | All trips | Own trips | All trips |
| Collect payment | No | Yes | Published/departed own trips | Yes |
| Correct paid to unpaid | No | No | No | Yes |
| Record cash refund | No | Yes | No | Yes |
| Edit trip fare / manage staff and fleet | No | No | No | Yes |

## API / Postman

Use the existing session-cookie and CSRF workflow. Mutations require `X-CSRF-TOKEN`.

- `GET /api/v1/counter/trips?page=0&size=20`: staff trip list.
- `GET /api/v1/counter/trips/{id}/manifest`: includes cancelled tickets so refunds remain visible. Driver manifests still contain confirmed tickets only.
- `POST /api/v1/counter/bookings`, with `Idempotency-Key`: `{ "tripId": "UUID", "seatNos": ["A1"], "contactName": "Walk-in Customer", "contactEmail": "", "contactPhone": "01700000000", "paymentStatus": "UNPAID", "expectedAmountMinor": 12500 }`. Returns 201; identical retries return 200 and the same booking. Keep the same key/body when the network result is uncertain. The sale dialog retains them while open; after closing an uncertain sale, check the manifest before issuing another ticket.
- `PATCH /api/v1/bookings/{id}/payment`: `{ "status": "PAID", "expectedStatus": "UNPAID", "reason": "Full cash collected at boarding" }`. Stale status produces 409; refresh the manifest. Use PAID → UNPAID for admin corrections and REFUND_DUE → REFUNDED for cash refunds.
- `PATCH /api/v1/admin/trips/{id}/fare`: `{ "fareMinor": 15000, "reason": "Updated fare schedule" }`.

`BookingView` adds `salesChannel` (ONLINE/COUNTER) and nullable `paymentUpdatedAt`. Method is PAY_ON_BOARD, CASH_COUNTER, or CASH_ON_BOARD. Payment status is UNPAID, PAID, REFUND_DUE, or REFUNDED. Existing endpoint paths remain available.

## Database and concurrency

V3 extends the accounts role constraint. Online bookings keep their passenger/hold relationships. Counter bookings have null passenger/hold and a required `sold_by_id` staff FK. A channel constraint enforces the two alternatives. `payment_updated_by_id` links the last payment actor; `payment_updated_at` records collection/correction/refund time. Audit events preserve each actor, action and reason. Cancellation is separately audited.

Counter sales, holds, confirmations, cancellation, payment changes and fare updates lock the trip row. Sales also lock the actor for idempotency. This prevents online/counter double sales and interleaving payment/cancellation changes. Requests validate the expected amount/status rather than silently overwriting a changed value. Existing Flyway V1/V2 files are unchanged.
