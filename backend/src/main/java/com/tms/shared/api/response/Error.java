package com.tms.shared.api.response;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public record Error(
    String code, String message, List<FieldError> fieldErrors, String traceId, Instant timestamp) {}
