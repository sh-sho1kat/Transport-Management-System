# Target roles and resource scope

Stage 0 implements no accounts or roles. Only GET /actuator/health is public; all other requests are denied. This table records target responsibilities, not currently granted permissions.

| Role | Intended scope |
|---|---|
| ADMIN | Identity/role administration and full operational administration, subject to business rules |
| MANAGER | Fleet, schedules, fares and operational reporting; no implicit identity administration |
| COUNTER_STAFF | Authorized counter sales, cash recording, ticket verification and check-in; refund/cancellation limits must follow explicit policies |
| PASSENGER | Public search and own bookings, payments, tickets and eligible cancellations |

Driver is a fleet record, not a login role. Do not infer login access from driver assignment. Preserve old DRIVER users through the reviewed mapping/archive process.

Implementation must define named permissions and an endpoint/action matrix in Stage 1 before granting access. New roles receive no implicit grants. Adding a role must not require role-name branches scattered through controllers. Apply both permission checks and ownership/resource checks in services, not only the UI.

Registration can create passenger accounts only; callers cannot choose privileged roles. Staff management requires an explicit administrative permission. Refresh tokens need expiry, revocation, rotation/reuse policy and tests. Never log credentials or tokens.

Account permissions do not override inventory locking, payment confirmation, cancellation deadlines or valid state transitions. Cash collection requires staff authorization and a recorded actor; money status is not a client-controlled booking flag.

Future counter/branch scope must be modeled before assuming staff have access to every location. Audit privileged changes when persistent audit is introduced.
