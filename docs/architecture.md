# Replacement architecture

A feature-based modular monolith: one Spring Boot application and one PostgreSQL database at eventual cutover. During migration the current backend remains on 8088, while backend-next is isolated on 8089. The existing frontend still uses the current backend.

## Target ownership

```text
com.tms/
  TmsApplication.java
  common/
    audit/                 timestamp auditing, not persistent business audit
    config/                clock, request tracing and application wiring
    exception/             central error mapping
    response/              transport response types
    security/              security configuration and shared security mechanisms
  auth/                    controller, dto, service, token
  user/                    user profiles, roles, repositories and policies
  station/                 stations
  route/                   routes and ordered route stops
  fleet/
    seatlayout/            SeatLayout and Seat
    bus/                   bus lifecycle
    driver/                independent Driver records
  trip/
    driver/                assignments
    fare/                  segment fares
    seat/                  snapshots and availability
    search/                search predicates/projections
  booking/
    passenger/             BookingPassenger
    allocation/            SeatAllocation
  payment/
    gateway/               mock first, real provider later
    refund/                refund policies and processing
  ticket/                  issuance, QR verification, check-in
```

Only common has implementation packages in Increment 0. Later extensions: reporting, audit, notification and fleet/maintenance. The application root remains com.tms, not the reference's placeholder com.yourname.busmanagement.

## MVC inside a feature

Use controller, dto/request, dto/response, entity, repository, service and mapper when needed. Keep enums with their owning entity as shown in the attached backend structure. Add policy or public api packages only where useful. Use concrete services; introduce interfaces for interchangeable implementations or actual module boundaries, not automatic Service/ServiceImpl pairs.

Controller → application service → owning repository. Return DTOs, never managed entities. Only the owning module writes its tables. Cross-module calls use documented public services/APIs, never another feature's repository/entity. Mapper code translates shapes without making business decisions.

## Dependency and transaction rules

Common contains no imports from business features. Authentication-specific adapters needing user data belong to auth/user and implement a common security interface if needed. Feature dependencies must remain acyclic. Avoid a common miscellaneous utility bucket.

The owning use-case service coordinates a cross-module transaction through public services. Payment confirmation atomically updates payment, booking, allocations and tickets; external gateway calls happen outside that critical transaction. Explicitly design an acyclic call graph before implementing confirmation/cancellation; do not solve a cycle by exposing repositories.

Lock the booking row consistently for confirmation/expiry, and lock TripSeat IDs deterministically for seat operations. Lock stable TripSeat rows, not only allocation rows that might not exist. Snapshot fares/seats where required. Reporting is read-only; notifications run after commit.

## Checks

ArchitectureTest scans production Java references for the allowed feature roots, controller/entity layer violations, common isolation, cross-feature service/API access and module cycles. Synthetic negative cases prove the checker rejects violations. This source-level guard does not replace transaction/concurrency tests, which accompany writable bookings.

## Incremental workflow

Follow PLAN.md and the current bounded task. Each vertical increment supplies migrations, services, DTOs/controllers, contracts and focused PostgreSQL tests. No empty future classes. No interface duplication. Current backend and frontend remain unchanged until a separately requested cutover.
