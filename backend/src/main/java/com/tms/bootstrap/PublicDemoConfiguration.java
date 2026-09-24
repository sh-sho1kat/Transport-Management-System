package com.tms.bootstrap;

import com.tms.catalog.dto.request.BusInput;
import com.tms.catalog.dto.request.RouteInput;
import com.tms.catalog.dto.request.SeatLayout;
import com.tms.catalog.dto.request.StopInput;
import com.tms.catalog.service.CatalogService;
import com.tms.identity.domain.Role;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.security.Actor;
import com.tms.identity.security.PublicDemoPolicy;
import com.tms.identity.service.AccountService;
import com.tms.scheduling.dto.request.TripInput;
import com.tms.scheduling.service.SchedulingService;
import java.time.*;
import java.util.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@ConditionalOnProperty(name = "app.demo-enabled", havingValue = "true")
public class PublicDemoConfiguration {
  @Bean
  ApplicationRunner publicDemoData(
      AccountRepository accounts,
      AccountService identity,
      CatalogService catalog,
      SchedulingService scheduling,
      Clock clock) {
    return args -> {
      ensure(
          accounts,
          identity,
          PublicDemoPolicy.ADMIN_EMAIL,
          PublicDemoPolicy.ADMIN_PASSWORD,
          "Demo Administrator",
          Role.ADMIN);
      ensure(
          accounts,
          identity,
          PublicDemoPolicy.PASSENGER_EMAIL,
          PublicDemoPolicy.PASSENGER_PASSWORD,
          "Demo Passenger",
          Role.PASSENGER);
      ensure(
          accounts,
          identity,
          "driver.demo@example.test",
          UUID.randomUUID() + "Demo!",
          "Demo Driver",
          Role.DRIVER);
      var actor = Actor.of(accounts.findByEmail(PublicDemoPolicy.ADMIN_EMAIL).orElseThrow());
      var context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(
          new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
      SecurityContextHolder.setContext(context);
      try {
        var stops = catalog.stops(true);
        var from =
            stops.stream()
                .filter(s -> s.name().equals("Central Demo Terminal"))
                .findFirst()
                .orElseGet(
                    () ->
                        catalog.saveStop(
                            null,
                            new StopInput(
                                "Central Demo Terminal",
                                "Dhaka",
                                "Fictional demo terminal",
                                true)));
        var to =
            stops.stream()
                .filter(s -> s.name().equals("Port Demo Terminal"))
                .findFirst()
                .orElseGet(
                    () ->
                        catalog.saveStop(
                            null,
                            new StopInput(
                                "Port Demo Terminal",
                                "Chattogram",
                                "Fictional demo terminal",
                                true)));
        var route =
            catalog.routes().stream()
                .filter(r -> r.code().equals("CV-DEMO-ROUTE"))
                .findFirst()
                .orElseGet(
                    () ->
                        catalog.saveRoute(
                            null,
                            new RouteInput(
                                "CV-DEMO-ROUTE",
                                "Dhaka to Chattogram",
                                true,
                                List.of(from.id(), to.id()))));
        var seats = new ArrayList<SeatLayout>();
        for (int i = 0; i < 40; i++)
          seats.add(
              new SeatLayout("" + (char) ('A' + i / 4) + (i % 4 + 1), i / 4 + 1, i % 4 + 1, false));
        var bus =
            catalog.buses().stream()
                .filter(b -> b.registration().equals("CV-DEMO-COACH"))
                .findFirst()
                .orElseGet(
                    () ->
                        catalog.saveBus(
                            null,
                            new BusInput("CV-DEMO-COACH", "Air-conditioned coach", true, seats)));
        var driver = accounts.findByEmail("driver.demo@example.test").orElseThrow();
        var existing = new HashSet<Instant>();
        int page = 0;
        while (true) {
          var result = scheduling.adminTrips(page, 100);
          result.items().stream()
              .filter(t -> t.routeId().equals(route.id()) && !t.status().name().equals("CANCELLED"))
              .forEach(t -> existing.add(t.departureAt()));
          if (++page >= result.totalPages()) break;
        }
        for (int day = 1; day <= 7; day++) {
          var departure =
              LocalDate.now(clock.withZone(ZoneId.of("Asia/Dhaka")))
                  .plusDays(day)
                  .atTime(9, 0)
                  .atZone(ZoneId.of("Asia/Dhaka"))
                  .toInstant();
          if (existing.contains(departure)) continue;
          var trip =
              scheduling.save(
                  null,
                  new TripInput(
                      route.id(),
                      bus.id(),
                      driver.getId(),
                      departure,
                      departure.plusSeconds(18000),
                      departure.minusSeconds(1800),
                      120000,
                      "BDT"));
          scheduling.publish(trip.id());
        }
      } finally {
        SecurityContextHolder.clearContext();
      }
    };
  }

  private static void ensure(
      AccountRepository accounts,
      AccountService identity,
      String email,
      String password,
      String name,
      Role role) {
    var found = accounts.findByEmail(email);
    if (found.isEmpty()) identity.create(email, password, name, "000-DEMO", role);
    else if (found.get().getRole() != role || !found.get().getActive())
      throw new IllegalStateException("Demo account configuration mismatch: " + email);
  }
}
