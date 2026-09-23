package com.tms.booking.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record CounterSale(
    @NotNull UUID tripId,
    @NotEmpty @Size(max = 4) List<@NotBlank @Size(max = 12) String> seatNos,
    @NotBlank @Size(max = 100) String contactName,
    @Email @Size(max = 254) String contactEmail,
    @NotBlank @Size(max = 30) String contactPhone,
    @NotBlank @Pattern(regexp = "PAID|UNPAID") String paymentStatus,
    @Min(1) @Max(400000000) long expectedAmountMinor) {}
