package com.tms.entity;

import com.tms.entity.Types.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "trips")
public class Trip extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_id")
  private Route route;

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route value) {
    route = value;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bus_id")
  private Bus bus;

  public Bus getBus() {
    return bus;
  }

  public void setBus(Bus value) {
    bus = value;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Account driver;

  public Account getDriver() {
    return driver;
  }

  public void setDriver(Account value) {
    driver = value;
  }

  private Instant departureAt;

  public Instant getDepartureAt() {
    return departureAt;
  }

  public void setDepartureAt(Instant value) {
    departureAt = value;
  }

  private Instant arrivalAt;

  public Instant getArrivalAt() {
    return arrivalAt;
  }

  public void setArrivalAt(Instant value) {
    arrivalAt = value;
  }

  private Instant reservedUntil;

  public Instant getReservedUntil() {
    return reservedUntil;
  }

  public void setReservedUntil(Instant value) {
    reservedUntil = value;
  }

  private Instant salesCloseAt;

  public Instant getSalesCloseAt() {
    return salesCloseAt;
  }

  public void setSalesCloseAt(Instant value) {
    salesCloseAt = value;
  }

  private long fareMinor;

  public long getFareMinor() {
    return fareMinor;
  }

  public void setFareMinor(long value) {
    fareMinor = value;
  }

  private String currency;

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String value) {
    currency = value;
  }

  @Enumerated(EnumType.STRING)
  private TripStatus status;

  public TripStatus getStatus() {
    return status;
  }

  public void setStatus(TripStatus value) {
    status = value;
  }

  private String originName;

  public String getOriginName() {
    return originName;
  }

  public void setOriginName(String value) {
    originName = value;
  }

  private String destinationName;

  public String getDestinationName() {
    return destinationName;
  }

  public void setDestinationName(String value) {
    destinationName = value;
  }

  private int cancellationHours;

  public int getCancellationHours() {
    return cancellationHours;
  }

  public void setCancellationHours(int value) {
    cancellationHours = value;
  }
}
