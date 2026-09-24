package com.tms.identity.security;

import com.tms.shared.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PublicDemoPolicy {
  public static final String ADMIN_EMAIL = "admin.demo@example.test";
  public static final String PASSENGER_EMAIL = "passenger.demo@example.test";
  public static final String ADMIN_PASSWORD = "DemoAdmin123!";
  public static final String PASSENGER_PASSWORD = "DemoPass123!";
  private final boolean enabled;

  public PublicDemoPolicy(@Value("${app.demo-enabled:false}") boolean enabled) {
    this.enabled = enabled;
  }

  public boolean enabled() {
    return enabled;
  }

  public boolean protectedAccount(String email) {
    return enabled
        && (ADMIN_EMAIL.equalsIgnoreCase(email) || PASSENGER_EMAIL.equalsIgnoreCase(email));
  }

  public void requireEditable(String email) {
    if (protectedAccount(email))
      throw new ApiException(
          403, "DEMO_ACCOUNT_PROTECTED", "Shared demo account details cannot be changed.");
  }

  public boolean denyRequest(String email, String method, String path) {
    if (!enabled || method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS"))
      return false;
    if (path.equals("/api/v1/auth/logout") || path.equals("/api/v1/auth/login")) return false;
    return ADMIN_EMAIL.equalsIgnoreCase(email);
  }
}
