package com.tms.shared.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record ReservationProperties(
    @Min(1) @Max(60) int holdMinutes,
    @Min(1) @Max(4) int maxSeats,
    @Min(0) @Max(168) int cancelHours,
    @Min(0) @Max(1440) int turnaroundMinutes,
    @NotBlank String timezone) {}
