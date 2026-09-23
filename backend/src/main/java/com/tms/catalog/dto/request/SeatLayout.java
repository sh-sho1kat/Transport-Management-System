package com.tms.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record SeatLayout(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,12}") String label,
    @Min(1) @Max(30) int rowNumber,
    @Min(1) @Max(6) int columnNumber,
    boolean blocked) {}
