package com.tms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FoundationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void healthIncludesWorkingDatabase() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    assertEquals(
        "wayline-next",
        jdbc.queryForObject(
            "select application from foundation_metadata where id=1", String.class));
  }

  @Test
  void futureApiIsClosedWithoutSession() throws Exception {
    mvc.perform(get("/api/v1/bookings"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.path").value("/api/v1/bookings"))
        .andExpect(jsonPath("$.traceId").isNotEmpty())
        .andExpect(header().doesNotExist("Set-Cookie"));
  }

  @Test
  void writesAndActuatorDetailsAreClosed() throws Exception {
    mvc.perform(post("/actuator/health")).andExpect(status().isUnauthorized());
    mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void authenticatedRequestsAreAlsoDeniedWithStandardErrors() throws Exception {
    var result =
        mvc.perform(get("/api/v1/bookings"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.code").value("FORBIDDEN"))
            .andExpect(jsonPath("$.timestamp").isString())
            .andExpect(jsonPath("$.length()").value(6))
            .andReturn();
    var body =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(result.getResponse().getContentAsString());
    assertEquals(result.getResponse().getHeader("X-Trace-Id"), body.get("traceId").asText());
  }
}
