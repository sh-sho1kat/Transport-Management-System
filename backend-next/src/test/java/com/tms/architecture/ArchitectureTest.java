package com.tms.architecture;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  private static final Set<String> ROOTS =
      Set.of(
          "common",
          "auth",
          "user",
          "station",
          "route",
          "fleet",
          "trip",
          "booking",
          "payment",
          "ticket",
          "reporting",
          "audit",
          "notification");

  private static void validate(Map<String, String> sources) {
    Map<String, Set<String>> edges = new HashMap<>();
    for (var source : sources.entrySet()) {
      String path = source.getKey();
      if (!path.contains("/")) {
        assertEquals("TmsApplication.java", path);
        continue;
      }
      String owner = path.split("/")[0];
      assertTrue(ROOTS.contains(owner), "Unknown module: " + path);
      var dependencies =
          Pattern.compile("\\bcom\\.tms\\.[A-Za-z0-9_.*]+").matcher(source.getValue());
      edges.computeIfAbsent(owner, k -> new HashSet<>());
      while (dependencies.find()) {
        String dependency = dependencies.group();
        String target = dependency.split("\\.")[2];
        assertFalse(dependency.endsWith(".*"), "Use explicit business imports: " + path);
        if (path.contains("/controller/"))
          assertFalse(
              dependency.contains(".repository.") || dependency.contains(".entity."),
              "Controller boundary: " + path);
        if (path.contains("/entity/"))
          assertFalse(
              dependency.contains(".controller.")
                  || dependency.contains(".service.")
                  || dependency.contains(".dto."),
              "Entity boundary: " + path);
        if (owner.equals("common")) assertEquals("common", target, "Common isolation: " + path);
        if (!owner.equals(target)) {
          assertTrue(ROOTS.contains(target), "Unknown dependency: " + dependency);
          if (!target.equals("common"))
            assertTrue(
                dependency.contains(".api.") || dependency.contains(".service."),
                "Use a public module service/API: " + path);
          edges.get(owner).add(target);
        }
      }
    }
    Set<String> complete = new HashSet<>();
    for (String module : edges.keySet()) visit(module, edges, new HashSet<>(), complete);
  }

  private static void visit(
      String module, Map<String, Set<String>> edges, Set<String> path, Set<String> complete) {
    if (complete.contains(module)) return;
    assertTrue(path.add(module), "Cyclic module dependency: " + path + " -> " + module);
    for (String target : edges.getOrDefault(module, Set.of())) visit(target, edges, path, complete);
    path.remove(module);
    complete.add(module);
  }

  @Test
  void productionSourcesFollowFeatureBoundaries() throws Exception {
    Path root = Path.of("src/main/java/com/tms");
    Map<String, String> sources = new HashMap<>();
    try (var files = Files.walk(root)) {
      for (var file : files.filter(p -> p.toString().endsWith(".java")).toList())
        sources.put(root.relativize(file).toString().replace('\\', '/'), Files.readString(file));
    }
    assertTrue(sources.size() >= 8, "Architecture scan must inspect implemented sources");
    validate(sources);
  }

  @Test
  void detectsCrossModuleCycles() {
    assertThrows(
        AssertionError.class,
        () ->
            validate(
                Map.of(
                    "booking/service/BookingService.java",
                    "import com.tms.payment.service.PaymentService;",
                    "payment/service/PaymentService.java",
                    "import com.tms.booking.service.BookingService;")));
  }

  @Test
  void detectsControllerAndCommonViolations() {
    assertThrows(
        AssertionError.class,
        () ->
            validate(
                Map.of(
                    "trip/controller/TripController.java",
                    "import com.tms.trip.repository.TripRepository;")));
    assertThrows(
        AssertionError.class,
        () ->
            validate(Map.of("common/config/Bad.java", "import com.tms.user.service.UserService;")));
  }
}
