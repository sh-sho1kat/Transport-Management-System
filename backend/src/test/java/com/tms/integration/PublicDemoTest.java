package com.tms.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tms.identity.domain.Role;
import com.tms.identity.entity.ResetToken;
import com.tms.identity.repository.ResetTokenRepository;
import com.tms.identity.security.PublicDemoPolicy;
import com.tms.shared.security.Digests;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.demo-enabled=true")
class PublicDemoTest extends IntegrationFixture {
  @Autowired
  @Qualifier("publicDemoData")
  ApplicationRunner seed;

  @Autowired ResetTokenRepository tokens;

  @Test
  void seededAccountsWorkAndRestartDoesNotDuplicateInventory() throws Exception {
    seed.run(new DefaultApplicationArguments());
    int trips = sql.queryForObject("select count(*) from trips", Integer.class);
    assertEquals(8, trips);
    seed.run(new DefaultApplicationArguments());
    assertEquals(trips, sql.queryForObject("select count(*) from trips", Integer.class));
    for (var entry :
        Map.of(
                PublicDemoPolicy.ADMIN_EMAIL,
                PublicDemoPolicy.ADMIN_PASSWORD,
                PublicDemoPolicy.PASSENGER_EMAIL,
                PublicDemoPolicy.PASSENGER_PASSWORD)
            .entrySet())
      request(
              null,
              "POST",
              "/api/v1/auth/login",
              Map.of("email", entry.getKey(), "password", entry.getValue()))
          .andExpect(status().isOk());
  }

  @Test
  void adminPreviewCanReadButCannotMutateAndCanLogout() throws Exception {
    var demo =
        identity.create(
            PublicDemoPolicy.ADMIN_EMAIL,
            PublicDemoPolicy.ADMIN_PASSWORD,
            "Demo",
            "000",
            Role.ADMIN);
    request(demo, "GET", "/api/v1/admin/trips", null).andExpect(status().isOk());
    request(demo, "POST", "/api/v1/admin/staff", Map.of())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("DEMO_READ_ONLY"));
    request(demo, "DELETE", "/api/v1/admin/stops/" + route, null).andExpect(status().isForbidden());
    request(demo, "POST", "/api/v1/auth/logout", null).andExpect(status().isOk());
  }

  @Test
  void passengerCanHoldSeatsButCannotChangeSharedIdentity() throws Exception {
    var demo =
        identity.create(
            PublicDemoPolicy.PASSENGER_EMAIL,
            PublicDemoPolicy.PASSENGER_PASSWORD,
            "Demo",
            "000",
            Role.PASSENGER);
    request(demo, "POST", "/api/v1/holds", Map.of("tripId", trip, "seatNos", java.util.List.of("A1")))
        .andExpect(status().isCreated());
    request(demo, "PATCH", "/api/v1/me", Map.of("displayName", "Changed", "phone", "123"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("DEMO_ACCOUNT_PROTECTED"));
    request(passenger, "PATCH", "/api/v1/me", Map.of("displayName", "Changed", "phone", "123"))
        .andExpect(status().isOk());
  }

  @Test
  void privateAdminKeepsWriteAccessButCannotDisablePublicAccount() throws Exception {
    var demo =
        identity.create(
            PublicDemoPolicy.ADMIN_EMAIL,
            PublicDemoPolicy.ADMIN_PASSWORD,
            "Demo",
            "000",
            Role.ADMIN);
    request(
            admin,
            "POST",
            "/api/v1/admin/staff",
            Map.of(
                "email",
                "new@example.test",
                "password",
                "ExamplePass123!",
                "displayName",
                "New Staff",
                "phone",
                "123",
                "role",
                "COUNTER_STAFF"))
        .andExpect(status().isCreated());
    request(
            admin,
            "PATCH",
            "/api/v1/admin/staff/" + demo.getId() + "/active",
            Map.of("active", false, "reason", "test"))
        .andExpect(status().isForbidden());
  }

  @Test
  void preexistingResetTokenCannotChangePublicPassword() throws Exception {
    var demo =
        identity.create(
            PublicDemoPolicy.PASSENGER_EMAIL,
            PublicDemoPolicy.PASSENGER_PASSWORD,
            "Demo",
            "000",
            Role.PASSENGER);
    var token = new ResetToken();
    token.setAccount(demo);
    token.setTokenHash(Digests.hash("test-token"));
    token.setExpiresAt(Instant.now().plusSeconds(600));
    tokens.saveAndFlush(token);
    request(
            null,
            "POST",
            "/api/v1/auth/password-resets",
            Map.of("token", "test-token", "password", "ChangedPass123!"))
        .andExpect(status().isForbidden());
    assertEquals(
        demo.getPasswordHash(), accounts.findById(demo.getId()).orElseThrow().getPasswordHash());
  }
}
