package com.tms.architecture;

import static org.junit.jupiter.api.Assertions.*;

import com.tms.identity.domain.Role;
import com.tms.identity.security.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PermissionPolicyTest {
  @Test
  void passengerHasNoStaffCapabilities() {
    assertEquals(Set.of(Permission.SELF_BOOK), RolePermissions.permissions(Role.PASSENGER));
  }

  @Test
  void counterCannotAdministerTripsStaffOrReverseCollections() {
    var grants = RolePermissions.permissions(Role.COUNTER_STAFF);
    assertTrue(
        grants.containsAll(
            Set.of(
                Permission.COUNTER_SELL,
                Permission.PAYMENT_COLLECT_ALL,
                Permission.PAYMENT_REFUND)));
    assertFalse(grants.contains(Permission.STAFF_MANAGE));
    assertFalse(grants.contains(Permission.TRIP_MANAGE));
    assertFalse(grants.contains(Permission.PAYMENT_CORRECT));
  }

  @Test
  void driverHasOnlyAssignedScope() {
    assertEquals(
        Set.of(Permission.TRIP_OPERATE_ASSIGNED, Permission.PAYMENT_COLLECT_ASSIGNED),
        RolePermissions.permissions(Role.DRIVER));
  }

  @Test
  void grantsAreImmutableAndEveryRoleIsDeliberate() {
    for (var role : Role.values()) {
      assertFalse(RolePermissions.permissions(role).isEmpty());
      assertThrows(
          UnsupportedOperationException.class, () -> RolePermissions.permissions(role).clear());
    }
  }
}
