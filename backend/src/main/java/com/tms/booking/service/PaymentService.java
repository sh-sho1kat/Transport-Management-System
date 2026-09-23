package com.tms.booking.service;

import com.tms.audit.service.AuditService;
import com.tms.booking.domain.BookingStatus;
import com.tms.booking.dto.request.PaymentUpdate;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.entity.Booking;
import com.tms.booking.mapper.BookingMapper;
import com.tms.booking.repository.BookingRepository;
import com.tms.identity.entity.Account;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.identity.security.RolePermissions;
import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.entity.Trip;
import com.tms.shared.error.ApiException;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {
  private final SeatInventory inventory;
  private final BookingRepository bookings;
  private final CurrentAccount current;
  private final AuditService audit;
  private final EntityManager em;
  private final Clock clock;

  public PaymentService(
      SeatInventory inventory,
      BookingRepository bookings,
      CurrentAccount current,
      AuditService audit,
      EntityManager em,
      Clock clock) {
    this.inventory = inventory;
    this.bookings = bookings;
    this.current = current;
    this.audit = audit;
    this.em = em;
    this.clock = clock;
  }

  public BookingView payment(UUID id, PaymentUpdate r) {
    Account a = current.get();
    if (!RolePermissions.allows(a.getRole(), Permission.PAYMENT_COLLECT_ALL)
        && !RolePermissions.allows(a.getRole(), Permission.PAYMENT_COLLECT_ASSIGNED))
      throw new ApiException(403, "FORBIDDEN", "Only authorized staff can record payments.");
    Booking b = bookings.findById(id).orElseThrow(ApiException::missing);
    Trip t = lock(b.getTrip().getId());
    em.refresh(b);
    if (!RolePermissions.allows(a.getRole(), Permission.PAYMENT_COLLECT_ALL)
        && (!t.getDriver().getId().equals(a.getId())
            || !Set.of(TripStatus.PUBLISHED, TripStatus.DEPARTED).contains(t.getStatus())))
      throw new ApiException(403, "FORBIDDEN", "Only your active assigned trips can be updated.");
    String before = b.getPaymentStatus();
    if (!before.equals(r.expectedStatus()))
      throw ApiException.conflict("PAYMENT_CHANGED", "Payment changed. Refresh before updating.");
    boolean collect =
        b.getStatus() == BookingStatus.CONFIRMED
            && before.equals("UNPAID")
            && r.status().equals("PAID");
    boolean correct =
        b.getStatus() == BookingStatus.CONFIRMED
            && before.equals("PAID")
            && r.status().equals("UNPAID")
            && RolePermissions.allows(a.getRole(), Permission.PAYMENT_CORRECT);
    boolean refund =
        b.getStatus() == BookingStatus.CANCELLED
            && before.equals("REFUND_DUE")
            && r.status().equals("REFUNDED")
            && RolePermissions.allows(a.getRole(), Permission.PAYMENT_REFUND);
    if (!collect && !correct && !refund)
      throw ApiException.conflict(
          "INVALID_PAYMENT_TRANSITION", "This payment change is not allowed.");
    b.setPaymentStatus(r.status());
    if (collect)
      b.setPaymentMethod(
          !RolePermissions.allows(a.getRole(), Permission.PAYMENT_COLLECT_ALL)
              ? "CASH_ON_BOARD"
              : "CASH_COUNTER");
    if (correct) b.setPaymentMethod("PAY_ON_BOARD");
    b.setPaymentUpdatedBy(a);
    b.setPaymentUpdatedAt(clock.instant());
    audit.record(a, "PAYMENT_UPDATED", id, before + " -> " + r.status() + ": " + r.reason());
    return BookingMapper.booking(b);
  }

  private Trip lock(UUID id) {
    return inventory.lock(id);
  }
}
