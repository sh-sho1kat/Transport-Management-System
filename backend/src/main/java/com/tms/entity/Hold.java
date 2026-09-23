package com.tms.entity;

import com.tms.entity.Types.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "holds")
public class Hold extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trip_id")
  private Trip trip;

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip value) {
    trip = value;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "passenger_id")
  private Account passenger;

  public Account getPassenger() {
    return passenger;
  }

  public void setPassenger(Account value) {
    passenger = value;
  }

  private Instant expiresAt;

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant value) {
    expiresAt = value;
  }

  private long amountMinor;

  public long getAmountMinor() {
    return amountMinor;
  }

  public void setAmountMinor(long value) {
    amountMinor = value;
  }

  private String currency;

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String value) {
    currency = value;
  }

  private int cancellationHours;

  public int getCancellationHours() {
    return cancellationHours;
  }

  public void setCancellationHours(int value) {
    cancellationHours = value;
  }

  @Enumerated(EnumType.STRING)
  private HoldStatus status;

  public HoldStatus getStatus() {
    return status;
  }

  public void setStatus(HoldStatus value) {
    status = value;
  }

  @ManyToMany
  @JoinTable(
      name = "hold_seats",
      joinColumns = @JoinColumn(name = "hold_id"),
      inverseJoinColumns = @JoinColumn(name = "seat_id"))
  private List<TripSeat> seats = new ArrayList<>();

  public List<TripSeat> getSeats() {
    return seats;
  }

  public void setSeats(List<TripSeat> value) {
    seats = value;
  }
}
