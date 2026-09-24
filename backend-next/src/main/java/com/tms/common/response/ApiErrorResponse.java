package com.tms.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record ApiErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    String traceId) {}
