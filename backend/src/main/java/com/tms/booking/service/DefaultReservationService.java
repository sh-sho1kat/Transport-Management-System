package com.tms.booking.service;

import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.request.HoldInput;
import com.tms.booking.dto.request.PaymentUpdate;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.Confirmation;
import com.tms.booking.dto.response.HoldView;
import com.tms.shared.api.response.PageResult;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class DefaultReservationService implements ReservationService {
  private final BookingService bookings;
  private final HoldService holds;
  private final CounterSalesService counter;
  private final PaymentService payments;

  public DefaultReservationService(
      BookingService bookings,
      HoldService holds,
      CounterSalesService counter,
      PaymentService payments) {
    this.bookings = bookings;
    this.holds = holds;
    this.counter = counter;
    this.payments = payments;
  }

  public Confirmation counterSale(String key, CounterSale input) {
    return counter.counterSale(key, input);
  }

  public BookingView payment(UUID id, PaymentUpdate input) {
    return payments.payment(id, input);
  }

  public HoldView hold(HoldInput input) {
    return holds.hold(input);
  }

  public HoldView holdDetail(UUID id) {
    return holds.holdDetail(id);
  }

  public void release(UUID id) {
    holds.release(id);
  }

  public Confirmation confirm(String key, BookingInput input) {
    return bookings.confirm(key, input);
  }

  public PageResult<BookingView> history(int page, int size) {
    return bookings.history(page, size);
  }

  public BookingView detail(UUID id) {
    return bookings.detail(id);
  }

  public BookingView cancel(UUID id, String reason) {
    return bookings.cancel(id, reason);
  }

  public List<BookingView> manifest(UUID tripId) {
    return bookings.manifest(tripId);
  }

  public void cancelTrip(UUID id, String reason) {
    bookings.cancelTrip(id, reason);
  }

  public void expireTrip(UUID id) {
    holds.expireTrip(id);
  }
}
