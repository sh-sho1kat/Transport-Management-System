package com.tms.architecture;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  private static final Set<String> ROOTS =
      Set.of(
          "config",
          "shared",
          "identity",
          "network",
          "fleet",
          "trip",
          "reservation",
          "finance",
          "ticketing",
          "workflow",
          "reporting",
          "audit",
          "notification");

  @Test
  void moduleAndMvcBoundaries() throws Exception {
    Path root = Path.of("src/main/java/com/tms");
    int count = 0;
    try (var files = Files.walk(root)) {
      for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        String relative = root.relativize(file).toString().replace('\\', '/');
        if (!relative.contains("/")) {
          assertEquals("TmsApplication.java", relative);
          continue;
        }
        String owner = relative.split("/")[0];
        assertTrue(ROOTS.contains(owner), relative);
        String source = Files.readString(file);
        var imports =
            Pattern.compile("(?:import\\s+(?:static\\s+)?)?(com\\.tms\\.[A-Za-z0-9_.*]+)")
                .matcher(source);
        while (imports.find()) {
          String dependency = imports.group(1);
          count++;
          assertFalse(dependency.endsWith(".*"), relative);
          if (relative.contains("/controller/"))
            assertFalse(
                dependency.contains(".repository.") || dependency.contains(".entity."), relative);
          String target = dependency.split("\\.")[2];
          if (!owner.equals("workflow") && !owner.equals("config"))
            assertNotEquals("workflow", target, relative);
          if (owner.equals("shared")) assertEquals("shared", target, relative);
          if (!target.equals(owner) && !target.equals("shared") && !owner.equals("config"))
            assertTrue(
                dependency.contains(".api."),
                "Cross-module access must use api: " + relative + " -> " + dependency);
        }
      }
    }
    assertTrue(count >= 2, "Scan must include real application dependencies");
  }
}
