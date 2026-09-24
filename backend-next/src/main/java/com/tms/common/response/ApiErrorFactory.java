package com.tms.common.response;

import com.tms.common.config.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorFactory {
  private final Clock clock;

  public ApiErrorFactory(Clock clock) {
    this.clock = clock;
  }

  public ApiErrorResponse create(
      HttpServletRequest request, int status, String code, String message) {
    return new ApiErrorResponse(
        Instant.now(clock),
        status,
        code,
        message,
        request.getRequestURI(),
        (String) request.getAttribute(RequestTraceFilter.ATTRIBUTE));
  }
}
