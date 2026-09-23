package com.tms.scheduling.mapper;

import com.tms.scheduling.dto.response.TripView;
import com.tms.scheduling.entity.Trip;
import java.util.*;

public final class SchedulingMapper {
  private SchedulingMapper() {}

  public static TripView trip(Trip t) {
    return new TripView(
        t.getId(),
        t.getRoute().getId(),
        t.getRoute().getName(),
        t.getBus().getId(),
        t.getBus().getRegistration(),
        t.getDriver().getId(),
        t.getDriver().getDisplayName(),
        t.getOriginName(),
        t.getDestinationName(),
        t.getDepartureAt(),
        t.getArrivalAt(),
        t.getSalesCloseAt(),
        t.getFareMinor(),
        t.getCurrency(),
        t.getStatus(),
        t.getCancellationHours());
  }
}
