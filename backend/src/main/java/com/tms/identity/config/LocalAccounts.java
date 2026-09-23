package com.tms.identity.config;

import com.tms.identity.domain.Role;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.service.AccountService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Profile;

/** Fictional accounts for the local profile; never resets an existing password. */
@Configuration
@Profile("local")
public class LocalAccounts {
  @Bean
  ApplicationRunner initializeLocalAccounts(AccountRepository accounts, AccountService service) {
    return args -> {
      if (!accounts.existsByEmail("counter.demo@example.test"))
        service.create(
            "counter.demo@example.test",
            "DemoPass123!",
            "Demo Counter Staff",
            "000-DEMO-COUNTER",
            Role.COUNTER_STAFF);
      if (!accounts.existsByEmail("passenger.demo@example.test"))
        service.create(
            "passenger.demo@example.test",
            "DemoPass123!",
            "Demo Passenger",
            "000-DEMO-PASSENGER",
            Role.PASSENGER);
      if (!accounts.existsByEmail("driver.demo@example.test"))
        service.create(
            "driver.demo@example.test",
            "DemoPass123!",
            "Demo Driver",
            "000-DEMO-DRIVER",
            Role.DRIVER);
    };
  }
}
