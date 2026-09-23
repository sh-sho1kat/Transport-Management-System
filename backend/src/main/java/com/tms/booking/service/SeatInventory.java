package com.tms.booking.service;

import com.tms.booking.domain.HoldStatus;
import com.tms.booking.entity.Hold;
import com.tms.booking.repository.HoldRepository;
import com.tms.scheduling.domain.SeatStatus;
import com.tms.scheduling.entity.Trip;
import com.tms.scheduling.entity.TripSeat;
import com.tms.scheduling.repository.TripRepository;
import com.tms.scheduling.repository.TripSeatRepository;
import com.tms.shared.error.ApiException;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
public class SeatInventory {
  private final TripRepository trips;
  private final TripSeatRepository seats;
  private final HoldRepository holds;
  private final Clock clock;

  public SeatInventory(
      TripRepository trips, TripSeatRepository seats, HoldRepository holds, Clock clock) {
    this.trips = trips;
    this.seats = seats;
    this.holds = holds;
    this.clock = clock;
  }

  public Trip lock(UUID id) {
    return trips.lockById(id).orElseThrow(ApiException::missing);
  }

  public void clear(Hold h, HoldStatus status) {
    for (TripSeat s : seats.findByHoldId(h.getId())) {
      s.setHold(null);
      s.setStatus(SeatStatus.AVAILABLE);
    }
    h.setStatus(status);
  }

  public void expire(UUID trip) {
    for (Hold h : holds.findByTripIdAndStatus(trip, HoldStatus.ACTIVE))
      if (!h.getExpiresAt().isAfter(clock.instant())) clear(h, HoldStatus.EXPIRED);
    seats.flush();
  }
}
