package com.tms.booking.service;

import com.tms.audit.service.AuditService;
import com.tms.booking.domain.BookingStatus;
import com.tms.booking.domain.HoldStatus;
import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.response.Confirmation;
import com.tms.booking.entity.Booking;
import com.tms.booking.entity.Hold;
import com.tms.booking.entity.IdempotencyRecord;
import com.tms.booking.mapper.BookingMapper;
import com.tms.booking.repository.BookingRepository;
import com.tms.booking.repository.IdempotencyRecordRepository;
import com.tms.identity.entity.Account;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.scheduling.domain.SeatStatus;
import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.entity.Trip;
import com.tms.scheduling.repository.TripSeatRepository;
import com.tms.shared.config.ReservationProperties;
import com.tms.shared.error.ApiException;
import com.tms.shared.security.Digests;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CounterSalesService {
  private final SeatInventory inventory;
  private final TripSeatRepository seats;
  private final BookingRepository bookings;
  private final AccountRepository accounts;
  private final IdempotencyRecordRepository keys;
  private final CurrentAccount current;
  private final AuditService audit;
  private final Clock clock;
  private final int maxSeats;

  public CounterSalesService(
      SeatInventory inventory,
      TripSeatRepository seats,
      BookingRepository bookings,
      AccountRepository accounts,
      IdempotencyRecordRepository keys,
      CurrentAccount current,
      AuditService audit,
      Clock clock,
      ReservationProperties settings) {
    this.inventory = inventory;
    this.seats = seats;
    this.bookings = bookings;
    this.accounts = accounts;
    this.keys = keys;
    this.current = current;
    this.audit = audit;
    this.clock = clock;
    this.maxSeats = settings.maxSeats();
  }

  private Account counter() {
    return current.require(Permission.COUNTER_SELL);
  }

  public Confirmation counterSale(String key, CounterSale r) {
    Account a = counter();
    a = accounts.lockById(a.getId()).orElseThrow(ApiException::missing);
    if (key == null || !key.matches("[A-Za-z0-9_-]{8,100}"))
      throw ApiException.invalid(
          "Use an Idempotency-Key of 8–100 letters, digits, underscores or hyphens.");
    String hash = Digests.hash("COUNTER|" + r.toString());
    var previous = keys.findByActorIdAndRequestKey(a.getId(), key);
    if (previous.isPresent()) {
      if (!previous.get().getRequestHash().equals(hash))
        throw ApiException.conflict(
            "IDEMPOTENCY_CONFLICT", "This key was used with a different request.");
      return new Confirmation(BookingMapper.booking(previous.get().getBooking()), true);
    }
    Trip t = lock(r.tripId());
    // Counter sales remain open until departure, even after online sales close.
    if (t.getStatus() != TripStatus.PUBLISHED || !clock.instant().isBefore(t.getDepartureAt()))
      throw ApiException.conflict("SALES_CLOSED", "Counter sales close at departure.");
    expire(t.getId());
    if (r.seatNos().isEmpty()
        || r.seatNos().size() > maxSeats
        || new HashSet<>(r.seatNos()).size() != r.seatNos().size())
      throw ApiException.invalid("Select distinct seats within the booking limit.");
    var selected =
        seats.findByTripIdOrderByRowNumberAscColumnNumberAsc(t.getId()).stream()
            .filter(s -> r.seatNos().contains(s.getLabel()))
            .toList();
    if (selected.size() != r.seatNos().size()
        || selected.stream().anyMatch(s -> s.getStatus() != SeatStatus.AVAILABLE))
      throw ApiException.conflict(
          "SEATS_UNAVAILABLE", "One or more selected seats are unavailable.");
    long amount = Math.multiplyExact(t.getFareMinor(), selected.size());
    if (amount != r.expectedAmountMinor())
      throw ApiException.conflict(
          "FARE_CHANGED", "The fare changed. Refresh and confirm the new total.");
    Booking b = new Booking();
    b.setReference("B-" + UUID.randomUUID().toString().replace("-", ""));
    b.setTrip(t);
    b.setSalesChannel("COUNTER");
    b.setSoldBy(a);
    b.setAmountMinor(amount);
    b.setCurrency(t.getCurrency());
    b.setContactName(r.contactName().trim());
    b.setContactEmail(
        r.contactEmail() == null ? "" : r.contactEmail().trim().toLowerCase(Locale.ROOT));
    b.setContactPhone(r.contactPhone().trim());
    b.setStatus(BookingStatus.CONFIRMED);
    b.setPaymentStatus(r.paymentStatus());
    b.setPaymentMethod(r.paymentStatus().equals("PAID") ? "CASH_COUNTER" : "PAY_ON_BOARD");
    if (r.paymentStatus().equals("PAID")) {
      b.setPaymentUpdatedBy(a);
      b.setPaymentUpdatedAt(clock.instant());
    }
    b.setOriginName(t.getOriginName());
    b.setDestinationName(t.getDestinationName());
    b.setDepartureAt(t.getDepartureAt());
    b.setCancellationHours(t.getCancellationHours());
    b.setSeats(new ArrayList<>(selected));
    bookings.saveAndFlush(b);
    for (var s : selected) {
      s.setBooking(b);
      s.setStatus(SeatStatus.BOOKED);
    }
    IdempotencyRecord record = new IdempotencyRecord();
    record.setActor(a);
    record.setRequestKey(key);
    record.setRequestHash(hash);
    record.setBooking(b);
    keys.save(record);
    audit.record(a, "COUNTER_SALE", b.getId(), "Cash status: " + r.paymentStatus());
    seats.flush();
    return new Confirmation(BookingMapper.booking(b), false);
  }

  private Trip lock(UUID id) {
    return inventory.lock(id);
  }

  private void clear(Hold h, HoldStatus status) {
    inventory.clear(h, status);
  }

  private void expire(UUID id) {
    inventory.expire(id);
  }
}
