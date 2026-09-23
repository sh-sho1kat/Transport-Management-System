package com.tms.identity.security;

import static com.tms.identity.security.Permission.*;

import com.tms.identity.domain.Role;
import java.util.Set;

/** Explicit grants: adding a role never silently grants it administrative access. */
public final class RolePermissions {
  private RolePermissions() {}

  public static Set<Permission> permissions(Role role) {
    return switch (role) {
      case PASSENGER -> Set.of(SELF_BOOK);
      case ADMIN ->
          Set.of(
              CATALOG_MANAGE,
              STAFF_MANAGE,
              TRIP_MANAGE,
              TRIP_READ_ALL,
              COUNTER_SELL,
              BOOKING_READ_ALL,
              BOOKING_CANCEL_OVERRIDE,
              MANIFEST_READ_ALL,
              PAYMENT_COLLECT_ALL,
              PAYMENT_CORRECT,
              PAYMENT_REFUND,
              REPORT_READ,
              AUDIT_READ);
      case DRIVER -> Set.of(TRIP_OPERATE_ASSIGNED, PAYMENT_COLLECT_ASSIGNED);
      case COUNTER_STAFF ->
          Set.of(
              TRIP_READ_ALL,
              COUNTER_SELL,
              BOOKING_READ_ALL,
              MANIFEST_READ_ALL,
              PAYMENT_COLLECT_ALL,
              PAYMENT_REFUND);
    };
  }

  public static boolean allows(Role role, Permission permission) {
    return permissions(role).contains(permission);
  }
}
