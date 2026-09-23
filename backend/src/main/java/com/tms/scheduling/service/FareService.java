package com.tms.scheduling.service;

import com.tms.audit.service.AuditService;
import com.tms.identity.entity.Account;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.dto.request.FareUpdate;
import com.tms.scheduling.dto.response.TripView;
import com.tms.scheduling.entity.Trip;
import com.tms.scheduling.mapper.SchedulingMapper;
import com.tms.scheduling.repository.TripRepository;
import com.tms.shared.error.ApiException;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FareService {
  private final TripRepository trips;
  private final CurrentAccount current;
  private final AuditService audit;
  private final Clock clock;

  public FareService(
      TripRepository trips, CurrentAccount current, AuditService audit, Clock clock) {
    this.trips = trips;
    this.current = current;
    this.audit = audit;
    this.clock = clock;
  }

  public TripView fare(UUID id, FareUpdate r) {
    Account a = current.require(Permission.TRIP_MANAGE);
    Trip t = lock(id);
    if (!Set.of(TripStatus.DRAFT, TripStatus.PUBLISHED).contains(t.getStatus())
        || !clock.instant().isBefore(t.getDepartureAt()))
      throw ApiException.conflict("FARE_CLOSED", "Fares can only change before departure.");
    long before = t.getFareMinor();
    t.setFareMinor(r.fareMinor());
    audit.record(a, "FARE_UPDATED", id, before + " -> " + r.fareMinor() + ": " + r.reason());
    return SchedulingMapper.trip(t);
  }

  private Trip lock(UUID id) {
    return trips.lockById(id).orElseThrow(ApiException::missing);
  }
}
