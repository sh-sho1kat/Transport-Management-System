package com.tms.booking.service;

import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.request.HoldInput;
import com.tms.booking.dto.request.PaymentUpdate;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.Confirmation;
import com.tms.booking.dto.response.HoldView;
import com.tms.shared.api.response.PageResult;
import java.util.*;

public interface ReservationService {
  Confirmation counterSale(String key, CounterSale input);

  BookingView payment(UUID id, PaymentUpdate input);

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
