package com.tms.booking.dto.response;

import com.tms.booking.domain.BookingStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

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
    int cancellationHours,
    String salesChannel,
    Instant paymentUpdatedAt) {}
