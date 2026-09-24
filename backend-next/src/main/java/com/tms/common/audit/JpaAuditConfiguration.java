package com.tms.common.audit;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditDateTimeProvider")
public class JpaAuditConfiguration {
  @Bean
  DateTimeProvider auditDateTimeProvider(Clock clock) {
    return () -> Optional.of(Instant.now(clock));
  }
}
