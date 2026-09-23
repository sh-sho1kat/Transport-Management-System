package com.tms.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.security.Actor;
import com.tms.shared.error.ApiErrors;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
  @Bean
  PasswordEncoder passwords() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  UserDetailsService users(AccountRepository accounts) {
    return email ->
        accounts
            .findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .map(Actor::of)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder passwords) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(passwords);
    return new ProviderManager(provider);
  }

  @Bean
  SecurityContextRepository contexts() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http,
      SecurityContextRepository contexts,
      AccountRepository accounts,
      ObjectMapper json,
      @Value("${app.allowed-origins}") String origins)
      throws Exception {
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
    cors.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "Idempotency-Key"));
    cors.setExposedHeaders(List.of("Idempotent-Replayed"));
    cors.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    http.cors(c -> c.configurationSource(source))
        .securityContext(c -> c.securityContextRepository(contexts))
        .requestCache(c -> c.disable())
        .formLogin(c -> c.disable())
        .httpBasic(c -> c.disable())
        .logout(c -> c.disable())
        .authorizeHttpRequests(
            c ->
                c.requestMatchers("/actuator/health", "/actuator/health/**", "/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.GET,
                        "/api/v1/trips",
                        "/api/v1/trips/*",
                        "/api/v1/trips/*/seats",
                        "/api/v1/stops")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .hasAnyAuthority(
                        "TRIP_READ_ALL",
                        "CATALOG_MANAGE",
                        "STAFF_MANAGE",
                        "TRIP_MANAGE",
                        "REPORT_READ",
                        "AUDIT_READ",
                        "BOOKING_CANCEL_OVERRIDE")
                    .requestMatchers("/api/v1/counter/**")
                    .hasAuthority("COUNTER_SELL")
                    .requestMatchers("/api/v1/driver/**")
                    .hasAuthority("TRIP_OPERATE_ASSIGNED")
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            c ->
                c.authenticationEntryPoint(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          json.writeValue(
                              res.getOutputStream(),
                              ApiErrors.body("UNAUTHENTICATED", "Please sign in", List.of()));
                        })
                    .accessDeniedHandler(
                        (req, res, e) -> {
                          res.setStatus(403);
                          res.setContentType("application/json");
                          json.writeValue(
                              res.getOutputStream(),
                              ApiErrors.body(
                                  "FORBIDDEN", "Access denied or CSRF token expired", List.of()));
                        }));
    http.addFilterAfter(
        new OncePerRequestFilter() {
          protected void doFilterInternal(
              HttpServletRequest req, HttpServletResponse res, FilterChain chain)
              throws ServletException, IOException {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Actor actor) {
              var a = accounts.findById(actor.id());
              if (a.isEmpty()
                  || !a.get().getActive()
                  || a.get().getAuthVersion() != actor.version()) {
                SecurityContextHolder.clearContext();
                if (req.getSession(false) != null) req.getSession(false).invalidate();
              }
            }
            chain.doFilter(req, res);
          }
        },
        AnonymousAuthenticationFilter.class);
    return http.build();
  }
}
