package com.tms.common.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tms.common.config.RequestTraceFilter;
import com.tms.common.response.ApiErrorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

class ErrorHandlingTest {
  @RestController
  static class Probe {
    @GetMapping("/probe")
    String fail() {
      throw new IllegalStateException("private database detail");
    }

    @PostMapping("/probe")
    Map<String, String> parse(@RequestBody Map<String, String> body) {
      return body;
    }

    @GetMapping("/forbidden")
    String denied() {
      throw new AccessDeniedException("internal permission details");
    }

    @GetMapping("/trace")
    Map<String, String> trace() {
      return Map.of("traceId", MDC.get("traceId"));
    }
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new Probe())
          .setControllerAdvice(
              new GlobalExceptionHandler(
                  new ApiErrorFactory(
                      Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC))))
          .addFilters(new RequestTraceFilter())
          .build();

  @Test
  void unexpectedErrorHasExactSafeContract() throws Exception {
    var result =
        mvc.perform(get("/probe"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.length()").value(6))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.timestamp").value("2026-01-01T00:00:00Z"))
            .andExpect(jsonPath("$.path").value("/probe"))
            .andReturn();
    String trace = result.getResponse().getHeader("X-Trace-Id");
    assertNotNull(trace);
    assertTrue(result.getResponse().getContentAsString().contains(trace));
    assertNull(MDC.get("traceId"));
  }

  @Test
  void malformedJsonAndWrongMethodHaveCorrectStatus() throws Exception {
    mvc.perform(post("/probe").contentType("application/json").content("{broken"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    mvc.perform(delete("/probe"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405));
  }

  @Test
  void forbiddenErrorsUseSameContract() throws Exception {
    mvc.perform(get("/forbidden"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.length()").value(6))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }

  @Test
  void serverOwnsTraceIdsAndClearsThreadContext() throws Exception {
    var first = mvc.perform(get("/trace").header("X-Trace-Id", "untrusted")).andReturn();
    String trace = first.getResponse().getHeader("X-Trace-Id");
    assertNotEquals("untrusted", trace);
    assertTrue(first.getResponse().getContentAsString().contains(trace));
    assertNull(MDC.get("traceId"));
    var next = mvc.perform(get("/trace")).andReturn();
    assertNotEquals(trace, next.getResponse().getHeader("X-Trace-Id"));
  }
}
