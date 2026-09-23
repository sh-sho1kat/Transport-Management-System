package com.tms.identity.config;

import com.tms.identity.domain.Role;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

@Configuration
public class AdminBootstrap {
  @Bean
  ApplicationRunner bootstrap(
      AccountRepository accounts,
      AccountService service,
      @Value("${app.admin-email}") String email,
      @Value("${app.admin-password}") String password) {
    return args -> {
      if (!email.isBlank()) {
        if (password.isBlank()) throw new IllegalStateException("Bootstrap password is required");
        if (!accounts.existsByEmail(email.trim().toLowerCase(java.util.Locale.ROOT)))
          service.create(email, password, "Administrator", "Not set", Role.ADMIN);
      }
    };
  }
}
