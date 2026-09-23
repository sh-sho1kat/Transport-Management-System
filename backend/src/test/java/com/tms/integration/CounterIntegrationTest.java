package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.request.PaymentUpdate;
import com.tms.identity.domain.Role;
import com.tms.identity.entity.Account;
import com.tms.identity.security.Actor;
import com.tms.scheduling.dto.request.FareUpdate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;

class CounterIntegrationTest extends IntegrationFixture {
  @Test
  void counterSaleIsAccountlessAtomicAndIdempotent() throws Exception {
    var counter = counterAccount();
    as(counter);
    var first = reservations.counterSale("counter-key-001", walkIn("PAID", "A1", "A2"));
    assertFalse(first.replayed());
    assertEquals("COUNTER", first.booking().salesChannel());
    assertEquals("PAID", first.booking().paymentStatus());
    assertEquals(25000, first.booking().amountMinor());
    assertEquals(
        0,
        sql.queryForObject(
            "select count(*) from bookings where passenger_id is not null", Integer.class));
    assertEquals(0, sql.queryForObject("select count(*) from holds", Integer.class));
    assertEquals(
        first.booking().id(),
        reservations.counterSale("counter-key-001", walkIn("PAID", "A1", "A2")).booking().id());
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> reservations.counterSale("counter-key-001", walkIn("UNPAID", "A1", "A2")));
    request(counter, "GET", "/api/v1/counter/trips", null).andExpect(status().isOk());
    request(counter, "GET", "/api/v1/admin/staff", null).andExpect(status().isForbidden());
    request(passenger, "GET", "/api/v1/bookings/" + first.booking().id(), null)
        .andExpect(status().isNotFound());
    request(counter, "GET", "/api/v1/bookings/" + first.booking().id() + "/ticket", null)
        .andExpect(status().isOk());
  }

  @Test
  void counterCannotSellHeldBlockedDuplicateOrStalePriceSeats() {
    hold(passenger, "A1");
    var counter = counterAccount();
    as(counter);
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> reservations.counterSale("counter-held", walkIn("UNPAID", "A1", "A2")));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> reservations.counterSale("counter-blocked", walkIn("UNPAID", "B2")));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> reservations.counterSale("counter-duplicate", walkIn("UNPAID", "A2", "A2")));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () ->
            reservations.counterSale(
                "counter-price",
                new CounterSale(trip, List.of("A2"), "Customer", null, "017", "PAID", 1)));
    assertEquals(
        "AVAILABLE",
        sql.queryForObject(
            "select status from trip_seats where trip_id=? and label='A2'", String.class, trip));
    assertEquals(0, sql.queryForObject("select count(*) from bookings", Integer.class));
  }

  @Test
  void counterSalesStayOpenUntilDeparture() {
    var counter = counterAccount();
    sql.update(
        "update trips set sales_close_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(60)),
        trip);
    as(counter);
    reservations.counterSale("counter-late-sale", walkIn("UNPAID", "A1"));
    sql.update(
        "update trips set departure_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(30)),
        trip);
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> reservations.counterSale("counter-departed", walkIn("UNPAID", "A2")));
  }

  @Test
  void paymentPermissionsAndStaleWritesAreEnforced() throws Exception {
    var b = book(passenger, "A1");
    var counter = counterAccount();
    var stranger =
        identity.create(
            "stranger@example.test", "ExamplePassword12", "Stranger", "015", Role.DRIVER);
    var paid = new PaymentUpdate("PAID", "UNPAID", "Cash received");
    request(passenger, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", paid)
        .andExpect(status().isForbidden());
    request(stranger, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", paid)
        .andExpect(status().isForbidden());
    request(driver, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", paid)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentMethod").value("CASH_ON_BOARD"));
    request(counter, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", paid)
        .andExpect(status().isConflict());
    var correction = new PaymentUpdate("UNPAID", "PAID", "Incorrect collection entry");
    request(counter, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", correction)
        .andExpect(status().isConflict());
    request(admin, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", correction)
        .andExpect(status().isOk());
    request(counter, "PATCH", "/api/v1/bookings/" + b.id() + "/payment", paid)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentMethod").value("CASH_COUNTER"));
    assertEquals(
        3,
        sql.queryForObject(
            "select count(*) from audit_events where action='PAYMENT_UPDATED'", Integer.class));
  }

  @Test
  void paidCancellationRequiresExplicitRefundRecord() throws Exception {
    var counter = counterAccount();
    as(counter);
    var b = reservations.counterSale("counter-paid-cancel", walkIn("PAID", "A1")).booking();
    assertEquals("REFUND_DUE", reservations.cancel(b.id(), "Customer cancelled").paymentStatus());
    request(counter, "GET", "/api/v1/counter/trips/" + trip + "/manifest", null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].paymentStatus").value("REFUND_DUE"));
    request(
            driver,
            "PATCH",
            "/api/v1/bookings/" + b.id() + "/payment",
            new PaymentUpdate("REFUNDED", "REFUND_DUE", "Cash returned"))
        .andExpect(status().isConflict());
    request(
            counter,
            "PATCH",
            "/api/v1/bookings/" + b.id() + "/payment",
            new PaymentUpdate("REFUNDED", "REFUND_DUE", "Cash returned"))
        .andExpect(status().isOk());
    request(
            counter,
            "PATCH",
            "/api/v1/bookings/" + b.id() + "/payment",
            new PaymentUpdate("PAID", "REFUNDED", "Not allowed"))
        .andExpect(status().isConflict());
  }

  @Test
  void fareChangesPreserveQuotesAndRequireAdmin() throws Exception {
    var b = book(passenger, "A1");
    var h = hold(other, "A2");
    var counter = counterAccount();
    request(
            counter,
            "PATCH",
            "/api/v1/admin/trips/" + trip + "/fare",
            new FareUpdate(15000, "New fare"))
        .andExpect(status().isForbidden());
    request(
            admin,
            "PATCH",
            "/api/v1/admin/trips/" + trip + "/fare",
            new FareUpdate(15000, "New fare"))
        .andExpect(status().isOk());
    as(other);
    assertEquals(
        12500, reservations.confirm("old-quote-key", contact(h.id())).booking().amountMinor());
    as(admin);
    assertEquals(12500, reservations.detail(b.id()).amountMinor());
    as(counter);
    assertEquals(
        15000,
        reservations
            .counterSale(
                "new-fare-key",
                new CounterSale(trip, List.of("B1"), "Guest", null, "017", "UNPAID", 15000))
            .booking()
            .amountMinor());
  }

  @Test
  void counterHttpRequiresRoleCsrfAndValidFields() throws Exception {
    var counter = counterAccount();
    mvc.perform(
            post("/api/v1/counter/bookings")
                .with(user(Actor.of(counter)))
                .header("Idempotency-Key", "counter-http-key")
                .contentType("application/json")
                .content(json.writeValueAsString(walkIn("UNPAID", "A1"))))
        .andExpect(status().isForbidden());
    for (Account a : List.of(passenger, driver))
      mvc.perform(
              post("/api/v1/counter/bookings")
                  .with(user(Actor.of(a)))
                  .with(csrf())
                  .header("Idempotency-Key", "counter-http-key")
                  .contentType("application/json")
                  .content(json.writeValueAsString(walkIn("UNPAID", "A1"))))
          .andExpect(status().isForbidden());
    var invalid = new CounterSale(trip, List.of("A1"), " ", "bad-email", "017", "OTHER", 12500);
    mvc.perform(
            post("/api/v1/counter/bookings")
                .with(user(Actor.of(counter)))
                .with(csrf())
                .header("Idempotency-Key", "counter-http-key")
                .contentType("application/json")
                .content(json.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
    for (int attempt = 0; attempt < 2; attempt++)
      mvc.perform(
              post("/api/v1/counter/bookings")
                  .with(user(Actor.of(counter)))
                  .with(csrf())
                  .header("Idempotency-Key", "counter-http-key")
                  .contentType("application/json")
                  .content(json.writeValueAsString(walkIn("UNPAID", "A1"))))
          .andExpect(status().is(attempt == 0 ? 201 : 200));
  }

  @Test
  void onlineAndCounterRaceHasOnlyOneWinner() throws Exception {
    var counter = counterAccount();
    var gate = new CountDownLatch(1);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var online =
          pool.submit(
              () -> {
                gate.await();
                try {
                  hold(passenger, "A1");
                  return true;
                } catch (com.tms.shared.error.ApiException e) {
                  return false;
                } finally {
                  SecurityContextHolder.clearContext();
                }
              });
      var offline =
          pool.submit(
              () -> {
                gate.await();
                try {
                  as(counter);
                  reservations.counterSale("counter-race-key", walkIn("UNPAID", "A1"));
                  return true;
                } catch (com.tms.shared.error.ApiException e) {
                  return false;
                } finally {
                  SecurityContextHolder.clearContext();
                }
              });
      gate.countDown();
      assertNotEquals(online.get(15, TimeUnit.SECONDS), offline.get(15, TimeUnit.SECONDS));
    }
  }
}
