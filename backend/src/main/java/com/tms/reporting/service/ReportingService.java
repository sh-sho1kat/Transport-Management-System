package com.tms.reporting.service;

import com.tms.audit.dto.response.AuditView;
import com.tms.audit.repository.AuditEventRepository;
import com.tms.booking.repository.BookingRepository;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.reporting.dto.response.Occupancy;
import com.tms.scheduling.repository.TripRepository;
import com.tms.scheduling.repository.TripSeatRepository;
import com.tms.shared.api.Pages;
import com.tms.shared.api.response.PageResult;
import com.tms.shared.persistence.PageRequests;
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
    current.require(Permission.REPORT_READ);
    var result = trips.findAll(PageRequests.paging(page, size, "departureAt"));
    var ids = result.getContent().stream().map(t -> t.getId()).toList();
    var inventory = new java.util.HashMap<java.util.UUID, TripSeatRepository.InventoryCount>();
    var totals = new java.util.HashMap<java.util.UUID, BookingRepository.BookingCount>();
    if (!ids.isEmpty()) {
      seats.aggregateInventory(ids).forEach(v -> inventory.put(v.getTripId(), v));
      bookings.aggregateBookings(ids).forEach(v -> totals.put(v.getTripId(), v));
    }
    return Pages.page(
        result,
        t -> {
          var stock = inventory.get(t.getId());
          var sales = totals.get(t.getId());
          long total = stock == null ? 0 : stock.getSellable(),
              booked = stock == null ? 0 : stock.getBooked();
          return new Occupancy(
              t.getId(),
              t.getRoute().getName(),
              t.getDepartureAt(),
              t.getCurrency(),
              total,
              booked,
              total == 0 ? 0 : Math.round(booked * 10000.0 / total) / 100.0,
              sales == null ? 0 : sales.getConfirmed(),
              sales == null ? 0 : sales.getCancelled(),
              sales == null ? 0 : sales.getValue());
        });
  }

  public PageResult<AuditView> audit(int page, int size) {
    current.require(Permission.AUDIT_READ);
    PageRequests.paging(page, size, "createdAt");
    return Pages.page(
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
