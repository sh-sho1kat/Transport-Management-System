package com.tms.service;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import java.util.*;

public interface ReservationService {
  HoldView hold(HoldInput input);

  HoldView holdDetail(UUID id);

  void release(UUID id);

  Confirmation confirm(String key, BookingInput input);

  PageResult<BookingView> history(int page, int size);

  BookingView detail(UUID id);

  BookingView cancel(UUID id, String reason);

  List<BookingView> manifest(UUID tripId);

  void cancelTrip(UUID id, String reason);

  void expireTrip(UUID id);
}
