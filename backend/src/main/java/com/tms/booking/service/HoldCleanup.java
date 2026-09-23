package com.tms.booking.service;

import com.tms.booking.domain.HoldStatus;
import com.tms.booking.repository.HoldRepository;
import java.time.Clock;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class HoldCleanup {
  private final HoldRepository holds;
  private final ReservationService reservations;
  private final Clock clock;

  public HoldCleanup(HoldRepository holds, ReservationService reservations, Clock clock) {
    this.holds = holds;
    this.reservations = reservations;
    this.clock = clock;
  }

  @Scheduled(fixedDelayString = "${app.cleanup-ms:30000}")
  public void cleanup() {
    for (var id : holds.expiredTripIds(HoldStatus.ACTIVE, clock.instant()))
      reservations.expireTrip(id);
  }
}
