package com.tms.service;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
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
