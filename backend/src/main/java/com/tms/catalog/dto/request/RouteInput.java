package com.tms.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record RouteInput(
    @NotBlank @Size(max = 40) String code,
    @NotBlank @Size(max = 100) String name,
    boolean active,
    @Size(min = 2, max = 30) @NotNull List<@NotNull UUID> stopIds) {}
