package com.tms.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record StopInput(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 255) String address,
    boolean active) {}
