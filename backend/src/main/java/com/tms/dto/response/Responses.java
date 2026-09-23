package com.tms.dto.response;

import com.tms.entity.Types.*;
import java.time.Instant;
import java.util.*;

public final class Responses {
  private Responses() {}

  public record Message(String message) {}

  public record User(
      UUID id, String email, String displayName, String phone, Role role, boolean active) {}

  public record Csrf(String token, String headerName) {}

  public record FieldError(String field, String message) {}

  public record Error(
      String code,
      String message,
      List<FieldError> fieldErrors,
      String traceId,
      Instant timestamp) {}

  public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {}

  public record StopView(UUID id, String name, String city, String address, boolean active) {}

  public record Layout(String label, int rowNumber, int columnNumber, boolean blocked) {}

  public record BusView(
      UUID id, String registration, String busType, boolean active, List<Layout> seats) {}

  public record RouteView(
      UUID id, String code, String name, boolean active, List<StopView> stops) {}

  public record TripView(
      UUID id,
      UUID routeId,
      String routeName,
      UUID busId,
      String busRegistration,
      UUID driverId,
      String driverName,
      String origin,
      String destination,
      Instant departureAt,
      Instant arrivalAt,
      Instant salesCloseAt,
      long fareMinor,
      String currency,
      TripStatus status,
      int cancellationHours) {}

  public record SeatView(String label, int rowNumber, int columnNumber, SeatStatus status) {}

  public record HoldView(
      UUID id,
      UUID tripId,
      HoldStatus status,
      List<String> seatNos,
      Instant expiresAt,
      long amountMinor,
      String currency,
      int cancellationHours) {}

  public record BookingView(
      UUID id,
      String reference,
      UUID tripId,
      List<String> seatNos,
      String origin,
      String destination,
      Instant departureAt,
      long amountMinor,
      String currency,
      BookingStatus status,
      String paymentMethod,
      String paymentStatus,
      String contactName,
      String contactEmail,
      String contactPhone,
      Instant createdAt,
      Instant cancelledAt,
      String cancellationReason,
      int cancellationHours) {}

  public record Confirmation(BookingView booking, boolean replayed) {}

  public record Occupancy(
      UUID tripId,
      String routeName,
      Instant departureAt,
      String currency,
      long sellableSeats,
      long bookedSeats,
      double occupancyPercent,
      long confirmedBookings,
      long cancelledBookings,
      long bookedValueMinor) {}

  public record AuditView(
      UUID id, String actor, String action, UUID resourceId, String reason, Instant occurredAt) {}
}
