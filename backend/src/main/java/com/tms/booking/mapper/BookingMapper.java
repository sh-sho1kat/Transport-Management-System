package com.tms.booking.mapper;

import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.HoldView;
import com.tms.booking.entity.Booking;
import com.tms.booking.entity.Hold;
import com.tms.scheduling.entity.TripSeat;
import java.util.*;

public final class BookingMapper {
  private BookingMapper() {}

  public static HoldView hold(Hold h) {
    return new HoldView(
        h.getId(),
        h.getTrip().getId(),
        h.getStatus(),
        labels(h.getSeats()),
        h.getExpiresAt(),
        h.getAmountMinor(),
        h.getCurrency(),
        h.getCancellationHours());
  }

  public static BookingView booking(Booking b) {
    return new BookingView(
        b.getId(),
        b.getReference(),
        b.getTrip().getId(),
        labels(b.getSeats()),
        b.getOriginName(),
        b.getDestinationName(),
        b.getDepartureAt(),
        b.getAmountMinor(),
        b.getCurrency(),
        b.getStatus(),
        b.getPaymentMethod(),
        b.getPaymentStatus(),
        b.getContactName(),
        b.getContactEmail(),
        b.getContactPhone(),
        b.getCreatedAt(),
        b.getCancelledAt(),
        b.getCancellationReason(),
        b.getCancellationHours(),
        b.getSalesChannel(),
        b.getPaymentUpdatedAt());
  }

  private static List<String> labels(List<TripSeat> seats) {
    return seats.stream().map(TripSeat::getLabel).sorted().toList();
  }
}
