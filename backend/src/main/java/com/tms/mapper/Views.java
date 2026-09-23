package com.tms.mapper;

import com.tms.dto.response.Responses.*;
import com.tms.entity.*;
import java.util.*;
import org.springframework.data.domain.Page;

public final class Views {
  private Views() {}

  public static User user(Account a) {
    return new User(
        a.getId(), a.getEmail(), a.getDisplayName(), a.getPhone(), a.getRole(), a.getActive());
  }

  public static StopView stop(Stop s) {
    return new StopView(s.getId(), s.getName(), s.getCity(), s.getAddress(), s.getActive());
  }

  public static BusView bus(Bus b, List<BusSeat> seats) {
    return new BusView(
        b.getId(),
        b.getRegistration(),
        b.getBusType(),
        b.getActive(),
        seats.stream()
            .map(
                s ->
                    new Layout(s.getLabel(), s.getRowNumber(), s.getColumnNumber(), s.getBlocked()))
            .toList());
  }

  public static RouteView route(Route r, List<RouteStop> stops) {
    return new RouteView(
        r.getId(),
        r.getCode(),
        r.getName(),
        r.getActive(),
        stops.stream().map(s -> stop(s.getStop())).toList());
  }

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
        b.getCancellationHours());
  }

  private static List<String> labels(List<TripSeat> seats) {
    return seats.stream().map(TripSeat::getLabel).sorted().toList();
  }

  public static <T, R> PageResult<R> page(Page<T> page, java.util.function.Function<T, R> mapper) {
    return new PageResult<>(
        page.getContent().stream().map(mapper).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
