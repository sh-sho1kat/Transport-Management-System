package com.tms.booking.dto.response;

import com.tms.booking.domain.HoldStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public record HoldView(
    UUID id,
    UUID tripId,
    HoldStatus status,
    List<String> seatNos,
    Instant expiresAt,
    long amountMinor,
    String currency,
    int cancellationHours) {}
