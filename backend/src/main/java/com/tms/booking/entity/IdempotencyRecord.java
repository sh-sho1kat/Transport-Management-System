package com.tms.booking.entity;

import com.tms.identity.entity.Account;
import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  private Account actor;

  public Account getActor() {
    return actor;
  }

  public void setActor(Account value) {
    actor = value;
  }

  private String requestKey;

  public String getRequestKey() {
    return requestKey;
  }

  public void setRequestKey(String value) {
    requestKey = value;
  }

  private String requestHash;

  public String getRequestHash() {
    return requestHash;
  }

  public void setRequestHash(String value) {
    requestHash = value;
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
