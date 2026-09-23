package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.booking.domain.BookingStatus;
import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.request.HoldInput;
import com.tms.identity.domain.Role;
import com.tms.identity.entity.Account;
import com.tms.shared.api.request.Reason;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;

class BookingIntegrationTest extends IntegrationFixture {
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
        com.tms.shared.error.ApiException.class,
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
        com.tms.shared.error.ApiException.class,
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
        com.tms.shared.error.ApiException.class,
        () ->
            reservations.confirm(
                "same-request",
                new BookingInput(h.id(), "Changed", "passenger@example.test", "012")));
    assertThrows(
        com.tms.shared.error.ApiException.class,
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
    assertThrows(
        com.tms.shared.error.ApiException.class, () -> reservations.cancel(b.id(), "Late"));
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
                  } catch (com.tms.shared.error.ApiException e) {
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
                  } catch (com.tms.shared.error.ApiException e) {
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
                } catch (com.tms.shared.error.ApiException e) {
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
  void reportingAggregatesConfirmedCancelledAndBlockedInventory() throws Exception {
    var confirmed = book(passenger, "A1");
    var cancelled = book(other, "A2");
    as(admin);
    reservations.cancel(cancelled.id(), "Report test");
    request(admin, "GET", "/api/v1/admin/reports/occupancy", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].sellableSeats").value(3))
        .andExpect(jsonPath("$.items[0].bookedSeats").value(1))
        .andExpect(jsonPath("$.items[0].confirmedBookings").value(1))
        .andExpect(jsonPath("$.items[0].cancelledBookings").value(1))
        .andExpect(jsonPath("$.items[0].bookedValueMinor").value(12500));
  }
}
