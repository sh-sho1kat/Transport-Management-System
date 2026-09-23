package com.tms.reporting.dto.response;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

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
