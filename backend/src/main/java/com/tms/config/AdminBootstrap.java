package com.tms.config;

import com.tms.entity.Types.Role;
import com.tms.repository.AccountRepository;
import com.tms.service.impl.AccountService;
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
