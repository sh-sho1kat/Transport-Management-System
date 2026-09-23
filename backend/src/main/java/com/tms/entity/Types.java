package com.tms.entity;

public final class Types {
  private Types() {}

  public enum Role {
    PASSENGER,
    ADMIN,
    DRIVER
  }

  public enum TripStatus {
    DRAFT,
    PUBLISHED,
    DEPARTED,
    COMPLETED,
    CANCELLED
  }

  public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED,
    BLOCKED
  }

  public enum HoldStatus {
    ACTIVE,
    CONSUMED,
    EXPIRED,
    RELEASED
  }

  public enum BookingStatus {
    CONFIRMED,
    CANCELLED
  }
}
