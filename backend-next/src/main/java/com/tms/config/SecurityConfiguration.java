package com.tms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.shared.response.ApiError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
  @Bean
  SecurityFilterChain security(HttpSecurity http, ObjectMapper mapper) throws Exception {
    // No login mechanism until Stage 1. Every non-health request is denied.
    return http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(org.springframework.http.HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (request, response, exception) -> {
                          response.setStatus(401);
                          response.setContentType("application/json");
                          mapper.writeValue(
                              response.getOutputStream(),
                              ApiError.of(
                                  "UNAUTHORIZED",
                                  "Authentication is not available in this foundation"));
                        })
                    .accessDeniedHandler(
                        (request, response, exception) -> {
                          response.setStatus(403);
                          response.setContentType("application/json");
                          mapper.writeValue(
                              response.getOutputStream(),
                              ApiError.of("FORBIDDEN", "Access denied"));
                        }))
        .build();
  }
}
