package com.tms.service.impl;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.entity.*;
import com.tms.entity.Types.*;
import com.tms.exception.ApiException;
import com.tms.mapper.Views;
import com.tms.repository.*;
import com.tms.security.CurrentAccount;
import com.tms.service.*;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService implements ReservationService {
  private final TripRepository trips;
  private final TripSeatRepository seats;
  private final HoldRepository holds;
  private final BookingRepository bookings;
  private final AccountRepository accounts;
  private final IdempotencyRecordRepository keys;
  private final CurrentAccount current;
  private final AuditService audit;
  private final EntityManager em;
  private final Clock clock;
  private final int minutes, maxSeats;

  public BookingService(
      TripRepository trips,
      TripSeatRepository seats,
      HoldRepository holds,
      BookingRepository bookings,
      AccountRepository accounts,
      IdempotencyRecordRepository keys,
      CurrentAccount current,
      AuditService audit,
      EntityManager em,
      Clock clock,
      @Value("${app.hold-minutes}") int minutes,
      @Value("${app.max-seats}") int maxSeats) {
    this.trips = trips;
    this.seats = seats;
    this.holds = holds;
    this.bookings = bookings;
    this.accounts = accounts;
    this.keys = keys;
    this.current = current;
    this.audit = audit;
    this.em = em;
    this.clock = clock;
    this.minutes = minutes;
    this.maxSeats = maxSeats;
  }

  private Trip lock(UUID id) {
    return trips.lockById(id).orElseThrow(ApiException::missing);
  }

  private Account passenger() {
    Account a = current.require(Role.PASSENGER);
    return accounts.lockById(a.getId()).orElseThrow(ApiException::missing);
  }

  private void selling(Trip t) {
    if (t.getStatus() != TripStatus.PUBLISHED || !t.getSalesCloseAt().isAfter(clock.instant()))
      throw ApiException.conflict("SALES_CLOSED", "This trip is not accepting bookings.");
  }

  private void clear(Hold h, HoldStatus status) {
    for (TripSeat s : seats.findByHoldId(h.getId())) {
      s.setHold(null);
      s.setStatus(SeatStatus.AVAILABLE);
    }
    h.setStatus(status);
  }

  private void expire(UUID trip) {
    for (Hold h : holds.findByTripIdAndStatus(trip, HoldStatus.ACTIVE))
      if (!h.getExpiresAt().isAfter(clock.instant())) clear(h, HoldStatus.EXPIRED);
    seats.flush();
  }

  public void expireTrip(UUID id) {
    lock(id);
    expire(id);
  }

  public HoldView hold(HoldInput r) {
    Account a = passenger();
    Trip t = lock(r.tripId());
    selling(t);
    expire(t.getId());
    if (r.seatNos().isEmpty()
        || r.seatNos().size() > maxSeats
        || new HashSet<>(r.seatNos()).size() != r.seatNos().size())
      throw ApiException.invalid("Select distinct seats within the booking limit.");
    if (holds.findByTripIdAndStatus(t.getId(), HoldStatus.ACTIVE).stream()
        .anyMatch(h -> h.getPassenger().getId().equals(a.getId())))
      throw ApiException.conflict(
          "ACTIVE_HOLD", "Release your current hold before selecting again.");
    var selected =
        seats.findByTripIdOrderByRowNumberAscColumnNumberAsc(t.getId()).stream()
            .filter(s -> r.seatNos().contains(s.getLabel()))
            .toList();
    if (selected.size() != r.seatNos().size()
        || selected.stream().anyMatch(s -> s.getStatus() != SeatStatus.AVAILABLE))
      throw ApiException.conflict(
          "SEATS_UNAVAILABLE", "One or more selected seats are unavailable.");
    Hold h = new Hold();
    h.setTrip(t);
    h.setPassenger(a);
    h.setStatus(HoldStatus.ACTIVE);
    h.setExpiresAt(
        clock.instant().plusSeconds(minutes * 60L).isBefore(t.getSalesCloseAt())
            ? clock.instant().plusSeconds(minutes * 60L)
            : t.getSalesCloseAt());
    h.setAmountMinor(Math.multiplyExact(t.getFareMinor(), selected.size()));
    h.setCurrency(t.getCurrency());
    h.setCancellationHours(t.getCancellationHours());
    h.setSeats(new ArrayList<>(selected));
    holds.saveAndFlush(h);
    for (var s : selected) {
      s.setStatus(SeatStatus.HELD);
      s.setHold(h);
    }
    seats.flush();
    return Views.hold(h);
  }

  private Hold ownHold(UUID id) {
    Account a = current.require(Role.PASSENGER);
    Hold h = holds.findById(id).orElseThrow(ApiException::missing);
    if (!h.getPassenger().getId().equals(a.getId())) throw ApiException.missing();
    return h;
  }

  public HoldView holdDetail(UUID id) {
    Hold h = ownHold(id);
    lock(h.getTrip().getId());
    em.refresh(h);
    expire(h.getTrip().getId());
    return Views.hold(h);
  }

  public void release(UUID id) {
    Hold h = ownHold(id);
    lock(h.getTrip().getId());
    em.refresh(h);
    if (h.getStatus() == HoldStatus.ACTIVE)
      clear(
          h, h.getExpiresAt().isAfter(clock.instant()) ? HoldStatus.RELEASED : HoldStatus.EXPIRED);
  }

  private String fingerprint(BookingInput r) {
    return AccountService.hash(
        r.holdId()
            + "|"
            + r.contactName().length()
            + ":"
            + r.contactName()
            + "|"
            + r.contactEmail().length()
            + ":"
            + r.contactEmail()
            + "|"
            + r.contactPhone().length()
            + ":"
            + r.contactPhone());
  }

  public Confirmation confirm(String key, BookingInput r) {
    if (key == null || !key.matches("[A-Za-z0-9_-]{8,100}"))
      throw ApiException.invalid(
          "Use an Idempotency-Key of 8–100 letters, digits, underscores or hyphens.");
    Account a = passenger();
    String hash = fingerprint(r);
    var previous = keys.findByActorIdAndRequestKey(a.getId(), key);
    if (previous.isPresent()) {
      var p = previous.get();
      if (!p.getRequestHash().equals(hash))
        throw ApiException.conflict(
            "IDEMPOTENCY_CONFLICT", "This key was already used with a different request.");
      return new Confirmation(Views.booking(p.getBooking()), true);
    }
    Hold h = ownHold(r.holdId());
    Trip t = lock(h.getTrip().getId());
    em.refresh(h);
    selling(t);
    if (h.getStatus() != HoldStatus.ACTIVE || !h.getExpiresAt().isAfter(clock.instant()))
      throw ApiException.conflict(
          "HOLD_EXPIRED_OR_USED", "This hold is no longer available. Select seats again.");
    var selected = seats.findByHoldId(h.getId());
    if (selected.size() != h.getSeats().size() || selected.isEmpty())
      throw ApiException.conflict("HOLD_INVALID", "This hold no longer owns its seats.");
    Booking b = new Booking();
    b.setReference("B-" + UUID.randomUUID().toString().replace("-", ""));
    b.setPassenger(a);
    b.setTrip(t);
    b.setHold(h);
    b.setAmountMinor(h.getAmountMinor());
    b.setCurrency(h.getCurrency());
    b.setContactName(r.contactName().trim());
    b.setContactEmail(r.contactEmail().trim().toLowerCase(Locale.ROOT));
    b.setContactPhone(r.contactPhone().trim());
    b.setStatus(BookingStatus.CONFIRMED);
    b.setPaymentMethod("PAY_ON_BOARD");
    b.setPaymentStatus("UNPAID");
    b.setOriginName(t.getOriginName());
    b.setDestinationName(t.getDestinationName());
    b.setDepartureAt(t.getDepartureAt());
    b.setCancellationHours(h.getCancellationHours());
    b.setSeats(new ArrayList<>(selected));
    bookings.saveAndFlush(b);
    for (var s : selected) {
      s.setHold(null);
      s.setBooking(b);
      s.setStatus(SeatStatus.BOOKED);
    }
    h.setStatus(HoldStatus.CONSUMED);
    IdempotencyRecord record = new IdempotencyRecord();
    record.setActor(a);
    record.setRequestKey(key);
    record.setRequestHash(hash);
    record.setBooking(b);
    keys.save(record);
    audit.record(a, "BOOKING_CONFIRMED", b.getId(), null);
    seats.flush();
    return new Confirmation(Views.booking(b), false);
  }

  public PageResult<BookingView> history(int page, int size) {
    Account a = current.require(Role.PASSENGER);
    return Views.page(
        bookings.findByPassengerId(a.getId(), TripService.paging(page, size, "createdAt")),
        Views::booking);
  }

  private Booking accessible(UUID id) {
    Account a = current.get();
    Booking b = bookings.findById(id).orElseThrow(ApiException::missing);
    if (a.getRole() != Role.ADMIN && !b.getPassenger().getId().equals(a.getId()))
      throw ApiException.missing();
    return b;
  }

  public BookingView detail(UUID id) {
    return Views.booking(accessible(id));
  }

  private void cancelBooking(Booking b, String reason, Account actor) {
    if (b.getStatus() == BookingStatus.CANCELLED) return;
    for (var s : seats.findByBookingId(b.getId())) {
      s.setBooking(null);
      s.setStatus(SeatStatus.AVAILABLE);
    }
    b.setStatus(BookingStatus.CANCELLED);
    b.setCancelledAt(clock.instant());
    b.setCancellationReason(reason);
    audit.record(actor, "BOOKING_CANCELLED", b.getId(), reason);
  }

  public BookingView cancel(UUID id, String reason) {
    Account a = current.get();
    Booking b = accessible(id);
    lock(b.getTrip().getId());
    em.refresh(b);
    if (b.getStatus() == BookingStatus.CANCELLED) return Views.booking(b);
    if (a.getRole() != Role.ADMIN
        && (b.getTrip().getStatus() != TripStatus.PUBLISHED
            || !clock
                .instant()
                .isBefore(b.getDepartureAt().minusSeconds(b.getCancellationHours() * 3600L))))
      throw ApiException.conflict("CANCELLATION_CLOSED", "The cancellation window has closed.");
    cancelBooking(b, reason, a);
    return Views.booking(b);
  }

  public List<BookingView> manifest(UUID id) {
    Account a = current.get();
    Trip t = trips.findById(id).orElseThrow(ApiException::missing);
    if (a.getRole() != Role.ADMIN
        && (a.getRole() != Role.DRIVER || !t.getDriver().getId().equals(a.getId())))
      throw ApiException.missing();
    return bookings.findByTripIdOrderByCreatedAt(id).stream()
        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
        .map(Views::booking)
        .toList();
  }

  public void cancelTrip(UUID id, String reason) {
    Account a = current.require(Role.ADMIN);
    Trip t = lock(id);
    if (t.getStatus() == TripStatus.CANCELLED) return;
    if (t.getStatus() == TripStatus.DEPARTED || t.getStatus() == TripStatus.COMPLETED)
      throw ApiException.conflict("INVALID_TRANSITION", "A departed trip cannot be cancelled.");
    for (Hold h : holds.findByTripIdAndStatus(id, HoldStatus.ACTIVE)) clear(h, HoldStatus.RELEASED);
    for (Booking b : bookings.findByTripIdOrderByCreatedAt(id)) cancelBooking(b, reason, a);
    t.setStatus(TripStatus.CANCELLED);
    audit.record(a, "TRIP_CANCELLED", id, reason);
  }
}
