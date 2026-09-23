package com.tms.booking.controller;

import com.tms.booking.dto.request.BookingInput;
import com.tms.booking.dto.request.HoldInput;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.dto.response.HoldView;
import com.tms.booking.service.ReservationService;
import com.tms.identity.security.CurrentAccount;
import com.tms.shared.api.request.Reason;
import com.tms.shared.api.response.PageResult;
import com.tms.shared.web.RateLimiter;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
  private final ReservationService service;
  private final RateLimiter limiter;
  private final CurrentAccount current;

  public BookingController(
      ReservationService service, RateLimiter limiter, CurrentAccount current) {
    this.service = service;
    this.limiter = limiter;
    this.current = current;
  }

  @PostMapping("/holds")
  @ResponseStatus(HttpStatus.CREATED)
  public HoldView hold(@Valid @RequestBody HoldInput r) {
    limiter.check("hold:" + current.get().getId(), 20);
    return service.hold(r);
  }

  @GetMapping("/holds/{id}")
  public HoldView hold(@PathVariable UUID id) {
    return service.holdDetail(id);
  }

  @DeleteMapping("/holds/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void release(@PathVariable UUID id) {
    service.release(id);
  }

  @PostMapping("/bookings")
  public ResponseEntity<BookingView> confirm(
      @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody BookingInput r) {
    var result = service.confirm(key, r);
    return ResponseEntity.status(result.replayed() ? 200 : 201)
        .header("Idempotent-Replayed", Boolean.toString(result.replayed()))
        .body(result.booking());
  }

  @GetMapping("/bookings")
  public PageResult<BookingView> history(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.history(page, size);
  }

  @GetMapping({"/bookings/{id}", "/bookings/{id}/ticket"})
  public BookingView detail(@PathVariable UUID id) {
    return service.detail(id);
  }

  @PostMapping("/bookings/{id}/cancel")
  public BookingView cancel(@PathVariable UUID id, @Valid @RequestBody Reason r) {
    return service.cancel(id, r.reason());
  }
}
