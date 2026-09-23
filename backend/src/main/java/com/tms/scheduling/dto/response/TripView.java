package com.tms.scheduling.dto.response;

import com.tms.scheduling.domain.TripStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

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
