package com.tms.entity;

import com.tms.entity.Types.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {
  private String salesChannel = "ONLINE";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sold_by_id")
  private Account soldBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_updated_by_id")
  private Account paymentUpdatedBy;

  private Instant paymentUpdatedAt;

  public String getSalesChannel() {
    return salesChannel;
  }

  public void setSalesChannel(String value) {
    salesChannel = value;
  }

  public Account getSoldBy() {
    return soldBy;
  }

  public void setSoldBy(Account value) {
    soldBy = value;
  }

  public Account getPaymentUpdatedBy() {
    return paymentUpdatedBy;
  }

  public void setPaymentUpdatedBy(Account value) {
    paymentUpdatedBy = value;
  }

  public Instant getPaymentUpdatedAt() {
    return paymentUpdatedAt;
  }

  public void setPaymentUpdatedAt(Instant value) {
    paymentUpdatedAt = value;
  }

  private String reference;

  public String getReference() {
    return reference;
  }

  public void setReference(String value) {
    reference = value;
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
  @JoinColumn(name = "hold_id")
  private Hold hold;

  public Hold getHold() {
    return hold;
  }

  public void setHold(Hold value) {
    hold = value;
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

  private String contactName;

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String value) {
    contactName = value;
  }

  private String contactEmail;

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String value) {
    contactEmail = value;
  }

  private String contactPhone;

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String value) {
    contactPhone = value;
  }

  @Enumerated(EnumType.STRING)
  private BookingStatus status;

  public BookingStatus getStatus() {
    return status;
  }

  public void setStatus(BookingStatus value) {
    status = value;
  }

  private String paymentMethod;

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String value) {
    paymentMethod = value;
  }

  private String paymentStatus;

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String value) {
    paymentStatus = value;
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

  private Instant departureAt;

  public Instant getDepartureAt() {
    return departureAt;
  }

  public void setDepartureAt(Instant value) {
    departureAt = value;
  }

  private int cancellationHours;

  public int getCancellationHours() {
    return cancellationHours;
  }

  public void setCancellationHours(int value) {
    cancellationHours = value;
  }

  private Instant cancelledAt;

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(Instant value) {
    cancelledAt = value;
  }

  private String cancellationReason;

  public String getCancellationReason() {
    return cancellationReason;
  }

  public void setCancellationReason(String value) {
    cancellationReason = value;
  }

  @ManyToMany
  @JoinTable(
      name = "booking_seats",
      joinColumns = @JoinColumn(name = "booking_id"),
      inverseJoinColumns = @JoinColumn(name = "seat_id"))
  private List<TripSeat> seats = new ArrayList<>();

  public List<TripSeat> getSeats() {
    return seats;
  }

  public void setSeats(List<TripSeat> value) {
    seats = value;
  }
}
