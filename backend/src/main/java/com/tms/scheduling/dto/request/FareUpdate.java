package com.tms.scheduling.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record FareUpdate(
    @Min(1) @Max(100000000) long fareMinor, @NotBlank @Size(max = 180) String reason) {}
