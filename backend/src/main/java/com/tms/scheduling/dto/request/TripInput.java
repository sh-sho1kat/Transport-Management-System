package com.tms.scheduling.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public record TripInput(
    @NotNull UUID routeId,
    @NotNull UUID busId,
    @NotNull UUID driverId,
    @NotNull Instant departureAt,
    @NotNull Instant arrivalAt,
    @NotNull Instant salesCloseAt,
    @Min(1) @Max(100000000) long fareMinor,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {}
