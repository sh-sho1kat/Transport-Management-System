package com.tms.booking.service;

import com.tms.audit.service.AuditService;
import com.tms.booking.domain.BookingStatus;
import com.tms.booking.domain.HoldStatus;
import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.Confirmation;
import com.tms.booking.entity.Booking;
import com.tms.booking.entity.Hold;
import com.tms.booking.entity.IdempotencyRecord;
import com.tms.booking.mapper.BookingMapper;
import com.tms.booking.repository.BookingRepository;
import com.tms.booking.repository.HoldRepository;
import com.tms.booking.repository.IdempotencyRecordRepository;
import com.tms.identity.entity.Account;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.identity.security.RolePermissions;
import com.tms.scheduling.domain.SeatStatus;
import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.entity.Trip;
import com.tms.scheduling.repository.TripRepository;
import com.tms.scheduling.repository.TripSeatRepository;
import com.tms.shared.api.Pages;
import com.tms.shared.api.response.PageResult;
import com.tms.shared.error.ApiException;
import com.tms.shared.persistence.PageRequests;
import com.tms.shared.security.Digests;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {
  private final SeatInventory inventory;
  private final HoldService holdService;
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

  public BookingService(
      SeatInventory inventory,
      HoldService holdService,
      TripRepository trips,
      TripSeatRepository seats,
      HoldRepository holds,
      BookingRepository bookings,
      AccountRepository accounts,
      IdempotencyRecordRepository keys,
      CurrentAccount current,
      AuditService audit,
      EntityManager em,
      Clock clock) {
    this.inventory = inventory;
    this.holdService = holdService;
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
  }

  private Account passenger() {
    Account a = current.require(Permission.SELF_BOOK);
    return accounts.lockById(a.getId()).orElseThrow(ApiException::missing);
  }

  private void selling(Trip t) {
    if (t.getStatus() != TripStatus.PUBLISHED || !t.getSalesCloseAt().isAfter(clock.instant()))
      throw ApiException.conflict("SALES_CLOSED", "This trip is not accepting bookings.");
  }

  private String fingerprint(BookingInput r) {
    return Digests.hash(
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
      return new Confirmation(BookingMapper.booking(p.getBooking()), true);
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
    return new Confirmation(BookingMapper.booking(b), false);
  }

  public PageResult<BookingView> history(int page, int size) {
    Account a = current.require(Permission.SELF_BOOK);
    return Pages.page(
        bookings.findByPassengerId(a.getId(), PageRequests.paging(page, size, "createdAt")),
        BookingMapper::booking);
  }

  private Booking accessible(UUID id) {
    Account a = current.get();
    Booking b = bookings.findById(id).orElseThrow(ApiException::missing);
    if (!RolePermissions.allows(a.getRole(), Permission.BOOKING_READ_ALL)
        && (b.getPassenger() == null || !b.getPassenger().getId().equals(a.getId())))
      throw ApiException.missing();
    return b;
  }

  public BookingView detail(UUID id) {
    return BookingMapper.booking(accessible(id));
  }

  private void cancelBooking(Booking b, String reason, Account actor) {
    if (b.getStatus() == BookingStatus.CANCELLED) return;
    for (var s : seats.findByBookingId(b.getId())) {
      s.setBooking(null);
      s.setStatus(SeatStatus.AVAILABLE);
    }
    if (b.getPaymentStatus().equals("PAID")) b.setPaymentStatus("REFUND_DUE");
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
    if (b.getStatus() == BookingStatus.CANCELLED) return BookingMapper.booking(b);
    if (!RolePermissions.allows(a.getRole(), Permission.BOOKING_CANCEL_OVERRIDE)
        && (b.getTrip().getStatus() != TripStatus.PUBLISHED
            || !clock
                .instant()
                .isBefore(b.getDepartureAt().minusSeconds(b.getCancellationHours() * 3600L))))
      throw ApiException.conflict("CANCELLATION_CLOSED", "The cancellation window has closed.");
    cancelBooking(b, reason, a);
    return BookingMapper.booking(b);
  }

  public List<BookingView> manifest(UUID id) {
    Account a = current.get();
    Trip t = trips.findById(id).orElseThrow(ApiException::missing);
    if (!RolePermissions.allows(a.getRole(), Permission.MANIFEST_READ_ALL)
        && (!RolePermissions.allows(a.getRole(), Permission.TRIP_OPERATE_ASSIGNED)
            || !t.getDriver().getId().equals(a.getId()))) throw ApiException.missing();
    return bookings.findByTripIdOrderByCreatedAt(id).stream()
        .filter(
            b ->
                RolePermissions.allows(a.getRole(), Permission.MANIFEST_READ_ALL)
                    || b.getStatus() == BookingStatus.CONFIRMED)
        .map(BookingMapper::booking)
        .toList();
  }

  public void cancelTrip(UUID id, String reason) {
    Account a = current.require(Permission.BOOKING_CANCEL_OVERRIDE);
    Trip t = lock(id);
    if (t.getStatus() == TripStatus.CANCELLED) return;
    if (t.getStatus() == TripStatus.DEPARTED || t.getStatus() == TripStatus.COMPLETED)
      throw ApiException.conflict("INVALID_TRANSITION", "A departed trip cannot be cancelled.");
    for (Hold h : holds.findByTripIdAndStatus(id, HoldStatus.ACTIVE)) clear(h, HoldStatus.RELEASED);
    for (Booking b : bookings.findByTripIdOrderByCreatedAt(id)) cancelBooking(b, reason, a);
    t.setStatus(TripStatus.CANCELLED);
    audit.record(a, "TRIP_CANCELLED", id, reason);
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

  private Hold ownHold(UUID id) {
    return holdService.ownHold(id);
  }
}
