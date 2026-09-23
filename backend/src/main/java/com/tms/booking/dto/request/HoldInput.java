package com.tms.booking.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record HoldInput(
    @NotNull UUID tripId,
    @NotEmpty @Size(max = 4) List<@NotBlank @Size(max = 12) String> seatNos) {}
