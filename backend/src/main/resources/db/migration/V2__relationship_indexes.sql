-- Index foreign-key lookups used inside the reservation transactions and manifests.
CREATE INDEX trip_seats_current_hold ON trip_seats(hold_id) WHERE hold_id IS NOT NULL;
CREATE INDEX trip_seats_current_booking ON trip_seats(booking_id) WHERE booking_id IS NOT NULL;
CREATE INDEX holds_trip_state ON holds(trip_id, status);
CREATE INDEX bookings_trip_created ON bookings(trip_id, created_at);
CREATE INDEX trips_driver_departure ON trips(driver_id, departure_at);
CREATE INDEX trips_bus_state ON trips(bus_id, status);
CREATE INDEX trips_route_state ON trips(route_id, status);
CREATE INDEX route_stops_stop ON route_stops(stop_id);
CREATE INDEX audit_events_created ON audit_events(created_at DESC, id);
