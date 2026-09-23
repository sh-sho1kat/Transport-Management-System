package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.tms.catalog.dto.request.StopInput;
import com.tms.identity.dto.request.Login;
import com.tms.identity.dto.request.Reset;
import com.tms.shared.api.request.Active;
import com.tms.shared.security.Digests;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;

class IdentityIntegrationTest extends IntegrationFixture {
  @Test
  void csrfCorsValidationAndRoleInjection() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            options("/api/v1/trips")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    mvc.perform(
            options("/api/v1/trips")
                .header("Origin", "https://untrusted.test")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/v1/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"email\":\"x@example.test\",\"password\":\"Password1234\",\"displayName\":\"X\",\"phone\":\"0\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isBadRequest());
    request(admin, "POST", "/api/v1/admin/stops", new StopInput("", "", "", true))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void realLoginLogoutAndPasswordResetRevokeSession() throws Exception {
    var csrfResult = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
    MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession();
    String token =
        json.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();
    String oldId = session.getId();
    mvc.perform(
            post("/api/v1/auth/login")
                .session(session)
                .header("X-CSRF-TOKEN", token)
                .contentType("application/json")
                .content(
                    json.writeValueAsString(new Login(passenger.getEmail(), "ExamplePassword12"))))
        .andExpect(status().isOk());
    assertNotEquals(oldId, session.getId());
    mvc.perform(get("/api/v1/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("PASSENGER"));
    sql.update(
        "insert into reset_tokens(id,created_at,account_id,token_hash,expires_at,consumed)"
            + " values(?,?,?,?,?,false)",
        UUID.randomUUID(),
        java.sql.Timestamp.from(Instant.now()),
        passenger.getId(),
        Digests.hash("test-reset-token"),
        java.sql.Timestamp.from(Instant.now().plusSeconds(600)));
    identity.reset(new Reset("test-reset-token", "NewPassword123"));
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> identity.reset(new Reset("test-reset-token", "NewPassword123")));
    mvc.perform(get("/api/v1/me").session(session)).andExpect(status().isUnauthorized());
  }

  @Test
  void staffDeactivationRevokesAccessAndCannotDisableSelf() throws Exception {
    as(admin);
    identity.active(driver.getId(), new Active(false, "Staff left operator"));
    SecurityContextHolder.clearContext();
    request(driver, "GET", "/api/v1/driver/trips", null).andExpect(status().isUnauthorized());
    as(admin);
    assertThrows(
        com.tms.shared.error.ApiException.class,
        () -> identity.active(admin.getId(), new Active(false, "Self")));
  }

  @Test
  void healthDoesNotRequireSmtpWhenRecoveryMailIsDisabled() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
