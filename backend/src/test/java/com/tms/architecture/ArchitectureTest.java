package com.tms.architecture;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  private static final Path ROOT = Path.of("src/main/java/com/tms");

  @Test
  void mvcDependenciesAndFeatureOwnershipStayExplicit() throws Exception {
    int checkedImports = 0;
    try (var files = Files.walk(ROOT)) {
      for (var file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        String text = Files.readString(file), relative = ROOT.relativize(file).toString();
        assertFalse(
            relative.matches("^(controller|service|entity|repository|dto|mapper)/.*"), relative);
        var imports = Pattern.compile("import (com\\.tms\\.[^;]+);").matcher(text);
        while (imports.find()) {
          checkedImports++;
          String dependency = imports.group(1);
          assertFalse(dependency.endsWith(".*"), "Explicit imports required: " + relative);
          if (relative.contains("/controller/"))
            assertFalse(
                dependency.contains(".repository.") || dependency.contains(".entity."),
                "Controller bypasses services: " + relative);
          if (relative.contains("/entity/") || relative.contains("/domain/"))
            assertFalse(
                dependency.contains(".service.")
                    || dependency.contains(".controller.")
                    || dependency.contains(".dto."),
                "Domain depends on application layer: " + relative);
          if (relative.startsWith("shared/"))
            assertTrue(
                dependency.startsWith("com.tms.shared."),
                "Shared code depends on feature: " + relative);
        }
        if (relative.contains("/service/") && !relative.startsWith("identity/"))
          assertFalse(
              text.matches("(?s).*\\bRole\\.[A-Z_]+.*"),
              "Use permissions, not hard-coded roles: " + relative);
      }
    }
    assertTrue(checkedImports > 100, "Architecture scan must inspect real dependencies");
  }

  @Test
  void inventoryRequiresAnExistingTransaction() {
    var annotation =
        com.tms.booking.service.SeatInventory.class.getAnnotation(
            org.springframework.transaction.annotation.Transactional.class);
    assertNotNull(annotation);
    assertEquals(
        org.springframework.transaction.annotation.Propagation.MANDATORY, annotation.propagation());
  }
}
