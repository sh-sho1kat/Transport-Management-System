package com.tms.catalog.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

public record BusInput(
    @NotBlank @Size(max = 40) String registration,
    @NotBlank @Size(max = 60) String busType,
    boolean active,
    @NotEmpty @Size(max = 100) List<@Valid SeatLayout> seats) {}
