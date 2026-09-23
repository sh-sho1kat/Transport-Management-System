package com.tms.booking.controller;

import com.tms.booking.dto.request.CounterSale;
import com.tms.booking.dto.request.PaymentUpdate;
import com.tms.booking.dto.response.BookingView;
import com.tms.booking.service.ReservationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CounterController {
  private final ReservationService service;

  public CounterController(ReservationService service) {
    this.service = service;
  }

  @PostMapping("/counter/bookings")
  public ResponseEntity<BookingView> sell(
      @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody CounterSale r) {
    var result = service.counterSale(key, r);
    return ResponseEntity.status(result.replayed() ? 200 : 201)
        .header("Idempotent-Replayed", Boolean.toString(result.replayed()))
        .body(result.booking());
  }

  @PatchMapping("/bookings/{id}/payment")
  public BookingView payment(@PathVariable UUID id, @Valid @RequestBody PaymentUpdate r) {
    return service.payment(id, r);
  }
}
