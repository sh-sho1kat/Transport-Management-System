package com.tms.config;

import com.tms.entity.Types.Role;
import com.tms.repository.AccountRepository;
import com.tms.service.impl.AccountService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

/** Fictional accounts for the local profile; never resets an existing password. */
@Configuration
@Profile("local")
public class LocalAccounts {
  @Bean
  ApplicationRunner initializeLocalAccounts(AccountRepository accounts, AccountService service) {
    return args -> {
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
