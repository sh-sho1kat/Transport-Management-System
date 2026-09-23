CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE TABLE accounts (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, email varchar(254) NOT NULL UNIQUE,
 display_name varchar(100) NOT NULL, phone varchar(30) NOT NULL, password_hash varchar(255) NOT NULL,
 role varchar(20) NOT NULL CHECK(role IN ('PASSENGER','ADMIN','DRIVER')), active boolean NOT NULL DEFAULT true,
 auth_version integer NOT NULL DEFAULT 0
);
CREATE TABLE buses (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, registration varchar(40) NOT NULL UNIQUE,
 bus_type varchar(60) NOT NULL, active boolean NOT NULL DEFAULT true
);
CREATE TABLE bus_seats (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, bus_id uuid NOT NULL REFERENCES buses(id),
 label varchar(12) NOT NULL, row_number integer NOT NULL CHECK(row_number>0), column_number integer NOT NULL CHECK(column_number>0),
 blocked boolean NOT NULL, UNIQUE(bus_id,label), UNIQUE(bus_id,row_number,column_number)
);
CREATE TABLE stops (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, name varchar(100) NOT NULL, city varchar(100) NOT NULL,
 address varchar(255) NOT NULL, active boolean NOT NULL DEFAULT true
);
CREATE TABLE routes (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, code varchar(40) NOT NULL UNIQUE,
 name varchar(100) NOT NULL, active boolean NOT NULL DEFAULT true
);
CREATE TABLE route_stops (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, route_id uuid NOT NULL REFERENCES routes(id),
 stop_id uuid NOT NULL REFERENCES stops(id), sequence integer NOT NULL CHECK(sequence>=0), UNIQUE(route_id,sequence), UNIQUE(route_id,stop_id)
);
CREATE TABLE trips (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, route_id uuid NOT NULL REFERENCES routes(id),
 bus_id uuid NOT NULL REFERENCES buses(id), driver_id uuid NOT NULL REFERENCES accounts(id),
 departure_at timestamptz NOT NULL, arrival_at timestamptz NOT NULL, reserved_until timestamptz NOT NULL,
 sales_close_at timestamptz NOT NULL, fare_minor bigint NOT NULL CHECK(fare_minor>0 AND fare_minor<=100000000),
 currency varchar(3) NOT NULL, status varchar(20) NOT NULL CHECK(status IN ('DRAFT','PUBLISHED','DEPARTED','COMPLETED','CANCELLED')),
 origin_name varchar(100), destination_name varchar(100), cancellation_hours integer NOT NULL,
 CHECK(arrival_at>departure_at), CHECK(reserved_until>=arrival_at), CHECK(sales_close_at<=departure_at),
 EXCLUDE USING gist (bus_id WITH =, tstzrange(departure_at,reserved_until,'[)') WITH &&) WHERE(status IN ('PUBLISHED','DEPARTED','COMPLETED')),
 EXCLUDE USING gist (driver_id WITH =, tstzrange(departure_at,reserved_until,'[)') WITH &&) WHERE(status IN ('PUBLISHED','DEPARTED','COMPLETED'))
);
CREATE INDEX trips_search ON trips(status,departure_at,route_id);
CREATE TABLE holds (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, trip_id uuid NOT NULL REFERENCES trips(id),
 passenger_id uuid NOT NULL REFERENCES accounts(id), expires_at timestamptz NOT NULL, amount_minor bigint NOT NULL,
 currency varchar(3) NOT NULL, cancellation_hours integer NOT NULL,
 status varchar(16) NOT NULL CHECK(status IN ('ACTIVE','CONSUMED','EXPIRED','RELEASED'))
);
CREATE INDEX hold_expiry ON holds(status,expires_at);
CREATE TABLE bookings (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, reference varchar(40) NOT NULL UNIQUE,
 passenger_id uuid NOT NULL REFERENCES accounts(id), trip_id uuid NOT NULL REFERENCES trips(id),
 hold_id uuid NOT NULL UNIQUE REFERENCES holds(id), amount_minor bigint NOT NULL, currency varchar(3) NOT NULL,
 contact_name varchar(100) NOT NULL, contact_email varchar(254) NOT NULL, contact_phone varchar(30) NOT NULL,
 status varchar(16) NOT NULL CHECK(status IN ('CONFIRMED','CANCELLED')),
 payment_method varchar(20) NOT NULL DEFAULT 'PAY_ON_BOARD', payment_status varchar(20) NOT NULL DEFAULT 'UNPAID',
 origin_name varchar(100) NOT NULL, destination_name varchar(100) NOT NULL, departure_at timestamptz NOT NULL,
 cancellation_hours integer NOT NULL, cancelled_at timestamptz, cancellation_reason varchar(255)
);
CREATE INDEX bookings_history ON bookings(passenger_id,created_at DESC);
CREATE TABLE trip_seats (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, trip_id uuid NOT NULL REFERENCES trips(id),
 label varchar(12) NOT NULL, row_number integer NOT NULL, column_number integer NOT NULL,
 status varchar(16) NOT NULL CHECK(status IN ('AVAILABLE','HELD','BOOKED','BLOCKED')),
 hold_id uuid REFERENCES holds(id), booking_id uuid REFERENCES bookings(id), UNIQUE(trip_id,label),
 CHECK((status='HELD' AND hold_id IS NOT NULL AND booking_id IS NULL)
    OR (status='BOOKED' AND booking_id IS NOT NULL AND hold_id IS NULL)
    OR (status IN ('AVAILABLE','BLOCKED') AND hold_id IS NULL AND booking_id IS NULL))
);
CREATE TABLE hold_seats (hold_id uuid REFERENCES holds(id), seat_id uuid REFERENCES trip_seats(id), PRIMARY KEY(hold_id,seat_id));
CREATE TABLE booking_seats (booking_id uuid REFERENCES bookings(id), seat_id uuid REFERENCES trip_seats(id), PRIMARY KEY(booking_id,seat_id));
CREATE TABLE idempotency_records (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, actor_id uuid NOT NULL REFERENCES accounts(id),
 request_key varchar(100) NOT NULL, request_hash varchar(64) NOT NULL, booking_id uuid NOT NULL REFERENCES bookings(id),
 UNIQUE(actor_id,request_key)
);
CREATE TABLE reset_tokens (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, account_id uuid NOT NULL REFERENCES accounts(id),
 token_hash varchar(64) NOT NULL UNIQUE, expires_at timestamptz NOT NULL, consumed boolean NOT NULL DEFAULT false
);
CREATE TABLE audit_events (
 id uuid PRIMARY KEY, created_at timestamptz NOT NULL, actor_id uuid REFERENCES accounts(id),
 action varchar(80) NOT NULL, resource_id uuid NOT NULL, reason varchar(255)
);
