package com.tms;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.entity.*;
import com.tms.entity.Types.*;
import com.tms.repository.*;
import com.tms.security.Actor;
import com.tms.service.*;
import com.tms.service.impl.AccountService;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationsIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate sql;
  @Autowired AccountService identity;
  @Autowired CatalogService catalog;
  @Autowired SchedulingService scheduling;
  @Autowired ReservationService reservations;
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

  @Test
  void publicSearchAndSeatMapContainNoPersonalInformation() throws Exception {
    book(passenger, "A1");
    SecurityContextHolder.clearContext();
    mvc.perform(get("/api/v1/trips"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItems").value(1));
    String map =
        mvc.perform(get("/api/v1/trips/" + trip + "/seats"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertFalse(map.contains("email"));
    assertFalse(map.contains("passenger"));
    assertTrue(map.contains("BOOKED"));
    assertTrue(map.contains("BLOCKED"));
  }

  @Test
  void rolesAndOwnershipEnforced() throws Exception {
    var b = book(passenger, "A1");
    SecurityContextHolder.clearContext();
    request(null, "GET", "/api/v1/bookings", null).andExpect(status().isUnauthorized());
    request(passenger, "GET", "/api/v1/admin/buses", null).andExpect(status().isForbidden());
    request(driver, "POST", "/api/v1/holds", new HoldInput(trip, List.of("A2")))
        .andExpect(status().isForbidden());
    request(other, "GET", "/api/v1/bookings/" + b.id(), null).andExpect(status().isNotFound());
    request(other, "POST", "/api/v1/bookings/" + b.id() + "/cancel", new Reason("Other user"))
        .andExpect(status().isNotFound());
    request(driver, "GET", "/api/v1/driver/trips/" + trip + "/manifest", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].reference").value(b.reference()));
    var second =
        identity.create(
            "driver2@example.test", "ExamplePassword12", "Second driver", "014", Role.DRIVER);
    request(second, "GET", "/api/v1/driver/trips/" + trip + "/manifest", null)
        .andExpect(status().isNotFound());
  }

  @Test
  void heldSeatsCannotBeTakenAndExpiryReleasesAll() {
    var h = hold(passenger, "A1", "A2");
    as(other);
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> reservations.hold(new HoldInput(trip, List.of("A2", "B1"))));
    assertEquals(
        "AVAILABLE",
        scheduling.seats(trip).stream()
            .filter(s -> s.label().equals("B1"))
            .findFirst()
            .orElseThrow()
            .status()
            .name());
    sql.update(
        "update holds set expires_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(1)),
        h.id());
    var next = hold(other, "A1", "A2");
    assertEquals(25000, next.amountMinor());
    as(passenger);
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> reservations.confirm("expired-key", contact(h.id())));
  }

  @Test
  void idempotencyHasStableBookingAndRejectsPayloadChanges() {
    var h = hold(passenger, "A1");
    var first = reservations.confirm("same-request", contact(h.id()));
    var replay = reservations.confirm("same-request", contact(h.id()));
    assertEquals(first.booking().id(), replay.booking().id());
    assertTrue(replay.replayed());
    assertThrows(
        com.tms.exception.ApiException.class,
        () ->
            reservations.confirm(
                "same-request",
                new BookingInput(h.id(), "Changed", "passenger@example.test", "012")));
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> reservations.confirm("different-key", contact(h.id())));
    assertEquals(1, sql.queryForObject("select count(*) from bookings", Integer.class));
  }

  @Test
  void cancellationIsIdempotentAndOldCancellationCannotReleaseNewBooking() {
    var b = book(passenger, "A1");
    as(passenger);
    assertEquals(BookingStatus.CANCELLED, reservations.cancel(b.id(), "Changed plans").status());
    var replacement = book(other, "A1");
    as(passenger);
    reservations.cancel(b.id(), "Retry");
    assertEquals(
        replacement.id(),
        sql.queryForObject(
            "select booking_id from trip_seats where trip_id=? and label='A1'", UUID.class, trip));
  }

  @Test
  void cutoffEnforcedButAdminOverrideAudited() {
    var b = book(passenger, "A1");
    sql.update(
        "update bookings set departure_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().plusSeconds(1800)),
        b.id());
    as(passenger);
    assertThrows(com.tms.exception.ApiException.class, () -> reservations.cancel(b.id(), "Late"));
    as(admin);
    reservations.cancel(b.id(), "Operator exception");
    assertEquals(
        1,
        sql.queryForObject(
            "select count(*) from audit_events where action='BOOKING_CANCELLED' and"
                + " reason='Operator exception'",
            Integer.class));
  }

  @Test
  void cancellingTripCancelsReservationsAndReleasesHolds() {
    var b = book(passenger, "A1");
    var h = hold(other, "A2");
    as(admin);
    reservations.cancelTrip(trip, "Bus unavailable");
    assertEquals(BookingStatus.CANCELLED, reservations.detail(b.id()).status());
    assertEquals(
        0,
        sql.queryForObject(
            "select count(*) from trip_seats where status in ('HELD','BOOKED')", Integer.class));
    as(other);
    assertEquals(HoldStatus.RELEASED, reservations.holdDetail(h.id()).status());
  }

  @Test
  void concurrentPassengersHaveExactlyOneWinner() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (Account a : List.of(passenger, other))
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  try {
                    hold(a, "A1");
                    return true;
                  } catch (com.tms.exception.ApiException e) {
                    assertEquals(409, e.status);
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      int wins = 0;
      for (var r : results) if (r.get(15, TimeUnit.SECONDS)) wins++;
      assertEquals(1, wins);
      assertEquals(
          1,
          sql.queryForObject("select count(*) from trip_seats where status='HELD'", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void concurrentConfirmRetriesCreateOneBooking() throws Exception {
    var h = hold(passenger, "A1");
    SecurityContextHolder.clearContext();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<UUID>> results = new ArrayList<>();
      for (int i = 0; i < 2; i++)
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  as(passenger);
                  try {
                    return reservations.confirm("concurrent-key", contact(h.id())).booking().id();
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      assertEquals(
          results.get(0).get(15, TimeUnit.SECONDS), results.get(1).get(15, TimeUnit.SECONDS));
      assertEquals(1, sql.queryForObject("select count(*) from bookings", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void postgresRejectsBusAndDriverOverlap() {
    as(admin);
    UUID overlap =
        scheduling.save(null, input(bus, driver.getId(), departure.plusSeconds(60))).id();
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () -> scheduling.publish(overlap));
    UUID secondBus =
        catalog
            .saveBus(
                null,
                new BusInput("BUS-2", "Coach", true, List.of(new SeatLayout("A1", 1, 1, false))))
            .id();
    UUID driverOverlap =
        scheduling.save(null, input(secondBus, driver.getId(), departure.plusSeconds(60))).id();
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () -> scheduling.publish(driverOverlap));
    assertEquals(
        "DRAFT", sql.queryForObject("select status from trips where id=?", String.class, overlap));
  }

  @Test
  void publishedResourcesAndTripAreProtected() {
    as(admin);
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> scheduling.save(trip, input(bus, driver.getId(), departure)));
    assertThrows(
        com.tms.exception.ApiException.class,
        () ->
            catalog.saveBus(
                bus,
                new BusInput("BUS-1", "Coach", true, List.of(new SeatLayout("X1", 1, 1, false)))));
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> scheduling.transition(trip, new Transition(TripStatus.DEPARTED)));
  }

  @Test
  void csrfCorsValidationAndRoleInjection() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            options("/api/v1/trips")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    mvc.perform(
            options("/api/v1/trips")
                .header("Origin", "https://untrusted.test")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/v1/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"email\":\"x@example.test\",\"password\":\"Password1234\",\"displayName\":\"X\",\"phone\":\"0\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isBadRequest());
    request(admin, "POST", "/api/v1/admin/stops", new StopInput("", "", "", true))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void realLoginLogoutAndPasswordResetRevokeSession() throws Exception {
    var csrfResult = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
    MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession();
    String token =
        json.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();
    String oldId = session.getId();
    mvc.perform(
            post("/api/v1/auth/login")
                .session(session)
                .header("X-CSRF-TOKEN", token)
                .contentType("application/json")
                .content(
                    json.writeValueAsString(new Login(passenger.getEmail(), "ExamplePassword12"))))
        .andExpect(status().isOk());
    assertNotEquals(oldId, session.getId());
    mvc.perform(get("/api/v1/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("PASSENGER"));
    sql.update(
        "insert into reset_tokens(id,created_at,account_id,token_hash,expires_at,consumed)"
            + " values(?,?,?,?,?,false)",
        UUID.randomUUID(),
        java.sql.Timestamp.from(Instant.now()),
        passenger.getId(),
        AccountService.hash("test-reset-token"),
        java.sql.Timestamp.from(Instant.now().plusSeconds(600)));
    identity.reset(new Reset("test-reset-token", "NewPassword123"));
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> identity.reset(new Reset("test-reset-token", "NewPassword123")));
    mvc.perform(get("/api/v1/me").session(session)).andExpect(status().isUnauthorized());
  }

  @Test
  void fiftySimultaneousClaimsHaveOneWinner() throws Exception {
    List<Account> actors = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      Account a = new Account();
      a.setEmail("load" + i + "@example.test");
      a.setDisplayName("Load test " + i);
      a.setPhone("0");
      a.setPasswordHash(passenger.getPasswordHash());
      a.setRole(Role.PASSENGER);
      actors.add(accounts.save(a));
    }
    ExecutorService pool = Executors.newFixedThreadPool(50);
    CountDownLatch ready = new CountDownLatch(50), start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (Account a : actors)
        results.add(
            pool.submit(
                () -> {
                  ready.countDown();
                  start.await();
                  try {
                    hold(a, "A1");
                    return true;
                  } catch (com.tms.exception.ApiException e) {
                    assertEquals(409, e.status);
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      assertTrue(ready.await(10, TimeUnit.SECONDS));
      start.countDown();
      int wins = 0;
      for (var r : results) if (r.get(30, TimeUnit.SECONDS)) wins++;
      assertEquals(1, wins);
      assertEquals(
          1, sql.queryForObject("select count(*) from holds where status='ACTIVE'", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void failedFinalWriteRollsBackBookingAndSeatChanges() {
    var h = hold(passenger, "A1", "A2");
    sql.execute(
        "ALTER TABLE idempotency_records ADD CONSTRAINT reject_test_write CHECK (false) NOT VALID");
    try {
      assertThrows(
          org.springframework.dao.DataIntegrityViolationException.class,
          () -> reservations.confirm("rollback-test", contact(h.id())));
    } finally {
      sql.execute("ALTER TABLE idempotency_records DROP CONSTRAINT reject_test_write");
    }
    assertEquals(0, sql.queryForObject("select count(*) from bookings", Integer.class));
    assertEquals(
        2,
        sql.queryForObject(
            "select count(*) from trip_seats where status='HELD' and hold_id=?",
            Integer.class,
            h.id()));
    assertEquals(
        "ACTIVE", sql.queryForObject("select status from holds where id=?", String.class, h.id()));
  }

  @Test
  void simultaneousPublicationCannotDoubleAssign() throws Exception {
    as(admin);
    UUID
        first = scheduling.save(null, input(bus, driver.getId(), departure.plusSeconds(7200))).id(),
        second =
            scheduling.save(null, input(bus, driver.getId(), departure.plusSeconds(7200))).id();
    SecurityContextHolder.clearContext();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (UUID id : List.of(first, second))
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  as(admin);
                  try {
                    scheduling.publish(id);
                    return true;
                  } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      int wins = 0;
      for (var r : results) if (r.get(15, TimeUnit.SECONDS)) wins++;
      assertEquals(1, wins);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void operatorCancellationRacingConfirmationLeavesNoLiveReservation() throws Exception {
    var h = hold(passenger, "A1");
    SecurityContextHolder.clearContext();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      var confirm =
          pool.submit(
              () -> {
                start.await();
                as(passenger);
                try {
                  reservations.confirm("race-confirm", contact(h.id()));
                } catch (com.tms.exception.ApiException e) {
                  assertEquals(409, e.status);
                } finally {
                  SecurityContextHolder.clearContext();
                }
                return true;
              });
      var cancel =
          pool.submit(
              () -> {
                start.await();
                as(admin);
                try {
                  reservations.cancelTrip(trip, "Cancelled during checkout");
                } finally {
                  SecurityContextHolder.clearContext();
                }
                return true;
              });
      start.countDown();
      confirm.get(15, TimeUnit.SECONDS);
      cancel.get(15, TimeUnit.SECONDS);
      assertEquals(
          0,
          sql.queryForObject(
              "select count(*) from bookings where status='CONFIRMED'", Integer.class));
      assertEquals(
          0,
          sql.queryForObject(
              "select count(*) from trip_seats where status in ('HELD','BOOKED')", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void staffDeactivationRevokesAccessAndCannotDisableSelf() throws Exception {
    as(admin);
    identity.active(driver.getId(), new Active(false, "Staff left operator"));
    SecurityContextHolder.clearContext();
    request(driver, "GET", "/api/v1/driver/trips", null).andExpect(status().isUnauthorized());
    as(admin);
    assertThrows(
        com.tms.exception.ApiException.class,
        () -> identity.active(admin.getId(), new Active(false, "Self")));
  }

  @Test
  void healthDoesNotRequireSmtpWhenRecoveryMailIsDisabled() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
