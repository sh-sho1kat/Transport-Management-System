package com.tms.dto.request;

import com.tms.entity.Types.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class Requests {
  private Requests() {}

  public record Register(
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 10, max = 72) String password,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(max = 30) String phone) {}

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}

  public record Recovery(@NotBlank @Email String email) {}

  public record Reset(
      @NotBlank String token, @NotBlank @Size(min = 10, max = 72) String password) {}

  public record Staff(
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 10, max = 72) String password,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(max = 30) String phone,
      @NotNull Role role) {}

  public record Profile(
      @NotBlank @Size(max = 100) String displayName, @NotBlank @Size(max = 30) String phone) {}

  public record SeatLayout(
      @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,12}") String label,
      @Min(1) @Max(30) int rowNumber,
      @Min(1) @Max(6) int columnNumber,
      boolean blocked) {}

  public record BusInput(
      @NotBlank @Size(max = 40) String registration,
      @NotBlank @Size(max = 60) String busType,
      boolean active,
      @NotEmpty @Size(max = 100) List<@Valid SeatLayout> seats) {}

  public record StopInput(
      @NotBlank @Size(max = 100) String name,
      @NotBlank @Size(max = 100) String city,
      @NotBlank @Size(max = 255) String address,
      boolean active) {}

  public record RouteInput(
      @NotBlank @Size(max = 40) String code,
      @NotBlank @Size(max = 100) String name,
      boolean active,
      @Size(min = 2, max = 30) @NotNull List<@NotNull UUID> stopIds) {}

  public record TripInput(
      @NotNull UUID routeId,
      @NotNull UUID busId,
      @NotNull UUID driverId,
      @NotNull Instant departureAt,
      @NotNull Instant arrivalAt,
      @NotNull Instant salesCloseAt,
      @Min(1) @Max(100000000) long fareMinor,
      @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {}

  public record HoldInput(
      @NotNull UUID tripId,
      @NotEmpty @Size(max = 4) List<@NotBlank @Size(max = 12) String> seatNos) {}

  public record BookingInput(
      @NotNull UUID holdId,
      @NotBlank @Size(max = 100) String contactName,
      @NotBlank @Email @Size(max = 254) String contactEmail,
      @NotBlank @Size(max = 30) String contactPhone) {}

  public record Reason(@NotBlank @Size(max = 255) String reason) {}

  public record Active(boolean active, @NotBlank @Size(max = 255) String reason) {}

  public record Transition(@NotNull TripStatus status) {}
}
