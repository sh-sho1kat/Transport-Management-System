package com.tms.scheduling.entity;

import com.tms.booking.entity.Booking;
import com.tms.booking.entity.Hold;
import com.tms.scheduling.domain.SeatStatus;
import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "trip_seats")
public class TripSeat extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trip_id")
  private Trip trip;

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip value) {
    trip = value;
  }

  private String label;

  public String getLabel() {
    return label;
  }

  public void setLabel(String value) {
    label = value;
  }

  private int rowNumber;

  public int getRowNumber() {
    return rowNumber;
  }

  public void setRowNumber(int value) {
    rowNumber = value;
  }

  private int columnNumber;

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setColumnNumber(int value) {
    columnNumber = value;
  }

  @Enumerated(EnumType.STRING)
  private SeatStatus status;

  public SeatStatus getStatus() {
    return status;
  }

  public void setStatus(SeatStatus value) {
    status = value;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id")
  private Booking booking;

  public Booking getBooking() {
    return booking;
  }

  public void setBooking(Booking value) {
    booking = value;
  }
}
