package com.tms.scheduling.service;

import com.tms.scheduling.dto.request.Transition;
import com.tms.scheduling.dto.request.TripInput;
import com.tms.scheduling.dto.response.SeatView;
import com.tms.scheduling.dto.response.TripView;
import com.tms.shared.api.response.PageResult;
import java.time.LocalDate;
import java.util.*;

public interface SchedulingService {
  PageResult<TripView> search(UUID origin, UUID destination, LocalDate date, int page, int size);

  TripView publicTrip(UUID id);

  List<SeatView> seats(UUID id);

  PageResult<TripView> adminTrips(int page, int size);

  TripView save(UUID id, TripInput input);

  TripView publish(UUID id);

  TripView transition(UUID id, Transition input);

  List<TripView> assigned();
}
