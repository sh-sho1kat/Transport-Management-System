package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.request.HoldInput;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.HoldView;
import com.tms.booking.service.ReservationService;
import com.tms.catalog.dto.request.BusInput;
import com.tms.catalog.dto.request.RouteInput;
import com.tms.catalog.dto.request.SeatLayout;
import com.tms.catalog.dto.request.StopInput;
import com.tms.catalog.service.CatalogService;
import com.tms.identity.domain.Role;
import com.tms.identity.entity.Account;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.security.Actor;
import com.tms.identity.service.AccountService;
import com.tms.scheduling.dto.request.TripInput;
import com.tms.scheduling.service.FareService;
import com.tms.scheduling.service.SchedulingService;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationFixture {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate sql;
  @Autowired AccountService identity;
  @Autowired CatalogService catalog;
  @Autowired SchedulingService scheduling;
  @Autowired ReservationService reservations;
  @Autowired FareService fares;
  @Autowired AccountRepository accounts;
  Account admin, driver, passenger, other;
  UUID route, bus, trip;
  Instant departure;

  @BeforeEach
  void fixture() {
    sql.execute(
        "TRUNCATE"
            + " audit_events,reset_tokens,idempotency_records,booking_seats,hold_seats,trip_seats,bookings,holds,trips,route_stops,routes,stops,bus_seats,buses,accounts"
            + " CASCADE");
    admin = identity.create("admin@example.test", "ExamplePassword12", "Admin", "010", Role.ADMIN);
    driver =
        identity.create("driver@example.test", "ExamplePassword12", "Driver", "011", Role.DRIVER);
    passenger =
        identity.create(
            "passenger@example.test", "ExamplePassword12", "Passenger", "012", Role.PASSENGER);
    other =
        identity.create("other@example.test", "ExamplePassword12", "Other", "013", Role.PASSENGER);
    as(admin);
    var origin = catalog.saveStop(null, new StopInput("Central", "City A", "Terminal A", true));
    var destination = catalog.saveStop(null, new StopInput("Harbor", "City B", "Terminal B", true));
    route =
        catalog
            .saveRoute(
                null,
                new RouteInput(
                    "R1", "Central to Harbor", true, List.of(origin.id(), destination.id())))
            .id();
    bus =
        catalog
            .saveBus(
                null,
                new BusInput(
                    "BUS-1",
                    "Coach",
                    true,
                    List.of(
                        new SeatLayout("A1", 1, 1, false),
                        new SeatLayout("A2", 1, 2, false),
                        new SeatLayout("B1", 2, 1, false),
                        new SeatLayout("B2", 2, 2, true))))
            .id();
    departure = Instant.now().plusSeconds(86400);
    trip = scheduling.save(null, input(bus, driver.getId(), departure)).id();
    scheduling.publish(trip);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  void as(Account a) {
    var actor = Actor.of(a);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
  }

  TripInput input(UUID busId, UUID driverId, Instant time) {
    return new TripInput(
        route, busId, driverId, time, time.plusSeconds(3600), time.minusSeconds(600), 12500, "BDT");
  }

  BookingInput contact(UUID hold) {
    return new BookingInput(hold, "Passenger", "passenger@example.test", "012");
  }

  HoldView hold(Account a, String... seats) {
    as(a);
    return reservations.hold(new HoldInput(trip, List.of(seats)));
  }

  BookingView book(Account a, String seat) {
    var h = hold(a, seat);
    return reservations.confirm(UUID.randomUUID().toString(), contact(h.id())).booking();
  }

  ResultActions request(Account a, String method, String path, Object body) throws Exception {
    var r =
        switch (method) {
          case "POST" -> post(path);
          case "PATCH" -> patch(path);
          case "DELETE" -> delete(path);
          default -> get(path);
        };
    if (a != null) r.with(user(Actor.of(a)));
    if (!method.equals("GET")) r.with(csrf());
    if (body != null) r.contentType("application/json").content(json.writeValueAsString(body));
    return mvc.perform(r);
  }

  Account counterAccount() {
    return identity.create(
        "counter@example.test", "ExamplePassword12", "Counter", "014", Role.COUNTER_STAFF);
  }

  CounterSale walkIn(String status, String... labels) {
    return new CounterSale(
        trip,
        List.of(labels),
        "Walk-in Customer",
        "",
        "01700000000",
        status,
        12500L * labels.length);
  }
}
