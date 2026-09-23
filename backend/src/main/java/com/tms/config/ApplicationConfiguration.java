package com.tms.config;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class ApplicationConfiguration {
  @Bean
  public Clock clock(
      @Value("${app.hold-minutes}") int holdMinutes,
      @Value("${app.max-seats}") int maxSeats,
      @Value("${app.cancel-hours}") int cancelHours,
      @Value("${app.turnaround-minutes}") int turnaroundMinutes) {
    if (holdMinutes < 1
        || holdMinutes > 60
        || maxSeats < 1
        || maxSeats > 4
        || cancelHours < 0
        || cancelHours > 168
        || turnaroundMinutes < 0
        || turnaroundMinutes > 1440)
      throw new IllegalArgumentException(
          "Invalid booking policy: holds 1–60 minutes, seats 1–4, cancellation 0–168 hours,"
              + " turnaround 0–1440 minutes");
    return Clock.systemUTC();
  }
}
