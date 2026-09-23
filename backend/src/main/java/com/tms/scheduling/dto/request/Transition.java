package com.tms.scheduling.dto.request;

import com.tms.scheduling.domain.TripStatus;
import jakarta.validation.constraints.*;
import java.util.*;

public record Transition(@NotNull TripStatus status) {}
