package com.tms.booking.service;

import com.tms.booking.domain.HoldStatus;
import com.tms.booking.dto.request.HoldInput;
import com.tms.booking.dto.response.HoldView;
import com.tms.booking.entity.Hold;
import com.tms.booking.mapper.BookingMapper;
import com.tms.booking.repository.HoldRepository;
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
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HoldService {
  private final SeatInventory inventory;
  private final TripSeatRepository seats;
  private final HoldRepository holds;
  private final AccountRepository accounts;
  private final CurrentAccount current;
  private final EntityManager em;
  private final Clock clock;
  private final int minutes;
  private final int maxSeats;

  public HoldService(
      SeatInventory inventory,
      TripSeatRepository seats,
      HoldRepository holds,
      AccountRepository accounts,
      CurrentAccount current,
      EntityManager em,
      Clock clock,
      ReservationProperties settings) {
    this.inventory = inventory;
    this.seats = seats;
    this.holds = holds;
    this.accounts = accounts;
    this.current = current;
    this.em = em;
    this.clock = clock;
    this.minutes = settings.holdMinutes();
    this.maxSeats = settings.maxSeats();
  }

  private Account passenger() {
    Account a = current.require(Permission.SELF_BOOK);
    return accounts.lockById(a.getId()).orElseThrow(ApiException::missing);
  }

  private void selling(Trip t) {
    if (t.getStatus() != TripStatus.PUBLISHED || !t.getSalesCloseAt().isAfter(clock.instant()))
      throw ApiException.conflict("SALES_CLOSED", "This trip is not accepting bookings.");
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
    return BookingMapper.hold(h);
  }

  public Hold ownHold(UUID id) {
    Account a = current.require(Permission.SELF_BOOK);
    Hold h = holds.findById(id).orElseThrow(ApiException::missing);
    if (!h.getPassenger().getId().equals(a.getId())) throw ApiException.missing();
    return h;
  }

  public HoldView holdDetail(UUID id) {
    Hold h = ownHold(id);
    lock(h.getTrip().getId());
    em.refresh(h);
    expire(h.getTrip().getId());
    return BookingMapper.hold(h);
  }

  public void release(UUID id) {
    Hold h = ownHold(id);
    lock(h.getTrip().getId());
    em.refresh(h);
    if (h.getStatus() == HoldStatus.ACTIVE)
      clear(
          h, h.getExpiresAt().isAfter(clock.instant()) ? HoldStatus.RELEASED : HoldStatus.EXPIRED);
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
