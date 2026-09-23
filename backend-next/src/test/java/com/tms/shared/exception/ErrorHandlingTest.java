package com.tms.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
  }

  @Test
  void internalDetailsAreNotReturned() throws Exception {
    MockMvcBuilders.standaloneSetup(new Probe())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build()
        .perform(get("/probe"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }

  @Test
  void malformedJsonReturnsSafeBadRequest() throws Exception {
    MockMvcBuilders.standaloneSetup(new Probe())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build()
        .perform(post("/probe").contentType("application/json").content("{broken"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }
}
