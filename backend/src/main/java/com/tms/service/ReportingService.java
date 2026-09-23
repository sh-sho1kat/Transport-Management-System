package com.tms.service;

import com.tms.dto.response.Responses.*;
import com.tms.entity.Types.*;
import com.tms.mapper.Views;
import com.tms.repository.*;
import com.tms.security.CurrentAccount;
import com.tms.service.impl.TripService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingService {
  private final TripRepository trips;
  private final TripSeatRepository seats;
  private final BookingRepository bookings;
  private final AuditEventRepository audit;
  private final CurrentAccount current;

  public ReportingService(
      TripRepository trips,
      TripSeatRepository seats,
      BookingRepository bookings,
      AuditEventRepository audit,
      CurrentAccount current) {
    this.trips = trips;
    this.seats = seats;
    this.bookings = bookings;
    this.audit = audit;
    this.current = current;
  }

  public PageResult<Occupancy> occupancy(int page, int size) {
    current.require(Role.ADMIN);
    return Views.page(
        trips.findAll(TripService.paging(page, size, "departureAt")),
        t -> {
          var inventory = seats.findByTripIdOrderByRowNumberAscColumnNumberAsc(t.getId());
          long total = inventory.stream().filter(s -> s.getStatus() != SeatStatus.BLOCKED).count(),
              booked = inventory.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
          var bs = bookings.findByTripIdOrderByCreatedAt(t.getId());
          long
              confirmed = bs.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count(),
              value =
                  bs.stream()
                      .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                      .mapToLong(b -> b.getAmountMinor())
                      .sum();
          return new Occupancy(
              t.getId(),
              t.getRoute().getName(),
              t.getDepartureAt(),
              t.getCurrency(),
              total,
              booked,
              total == 0 ? 0 : Math.round(booked * 10000.0 / total) / 100.0,
              confirmed,
              bs.size() - confirmed,
              value);
        });
  }

  public PageResult<AuditView> audit(int page, int size) {
    current.require(Role.ADMIN);
    TripService.paging(page, size, "createdAt");
    return Views.page(
        audit.findAll(
            org.springframework.data.domain.PageRequest.of(
                page,
                Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").descending().and(Sort.by("id")))),
        e ->
            new AuditView(
                e.getId(),
                e.getActor() == null ? "SYSTEM" : e.getActor().getDisplayName(),
                e.getAction(),
                e.getResourceId(),
                e.getReason(),
                e.getCreatedAt()));
  }
}
