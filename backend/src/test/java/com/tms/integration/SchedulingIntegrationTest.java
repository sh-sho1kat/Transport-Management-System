package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.booking.domain.BookingStatus;
import com.tms.booking.domain.HoldStatus;
import com.tms.catalog.dto.request.BusInput;
import com.tms.catalog.dto.request.SeatLayout;
import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.dto.request.Transition;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;

class SchedulingIntegrationTest extends IntegrationFixture {
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
        com.tms.shared.error.ApiException.class,
        () -> scheduling.save(trip, input(bus, driver.getId(), departure)));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () ->
            catalog.saveBus(
                bus,
                new BusInput("BUS-1", "Coach", true, List.of(new SeatLayout("X1", 1, 1, false)))));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> scheduling.transition(trip, new Transition(TripStatus.DEPARTED)));
  }
}
