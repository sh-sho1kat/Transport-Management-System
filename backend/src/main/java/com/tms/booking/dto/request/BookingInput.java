package com.tms.booking.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record BookingInput(
    @NotNull UUID holdId,
    @NotBlank @Size(max = 100) String contactName,
    @NotBlank @Email @Size(max = 254) String contactEmail,
    @NotBlank @Size(max = 30) String contactPhone) {}
