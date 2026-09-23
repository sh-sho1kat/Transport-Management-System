package com.tms.booking.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record PaymentUpdate(
    @NotBlank @Pattern(regexp = "PAID|UNPAID|REFUNDED") String status,
    @NotBlank @Pattern(regexp = "PAID|UNPAID|REFUND_DUE|REFUNDED") String expectedStatus,
    @NotBlank @Size(max = 180) String reason) {}
