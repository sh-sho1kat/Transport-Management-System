package com.tms.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {
  public static final String ATTRIBUTE = RequestTraceFilter.class.getName() + ".traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // Generate server-owned IDs; never trust arbitrary incoming header values.
    String traceId = UUID.randomUUID().toString();
    request.setAttribute(ATTRIBUTE, traceId);
    response.setHeader("X-Trace-Id", traceId);
    MDC.put("traceId", traceId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove("traceId");
    }
  }
}
