package com.tms.scheduling.controller;

import com.tms.booking.dto.response.BookingView;
import com.tms.booking.service.ReservationService;
import com.tms.scheduling.dto.request.FareUpdate;
import com.tms.scheduling.dto.request.Transition;
import com.tms.scheduling.dto.request.TripInput;
import com.tms.scheduling.dto.response.SeatView;
import com.tms.scheduling.dto.response.TripView;
import com.tms.scheduling.service.FareService;
import com.tms.scheduling.service.SchedulingService;
import com.tms.shared.api.request.Reason;
import com.tms.shared.api.response.Message;
import com.tms.shared.api.response.PageResult;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TripController {
  private final SchedulingService service;
  private final FareService fares;
  private final ReservationService reservations;

  public TripController(
      SchedulingService service, ReservationService reservations, FareService fares) {
    this.service = service;
    this.fares = fares;
    this.reservations = reservations;
  }

  @GetMapping("/trips")
  public PageResult<TripView> search(
      @RequestParam(required = false) UUID origin,
      @RequestParam(required = false) UUID destination,
      @RequestParam(required = false) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.search(origin, destination, date, page, size);
  }

  @GetMapping("/trips/{id}")
  public TripView trip(@PathVariable UUID id) {
    return service.publicTrip(id);
  }

  @GetMapping("/trips/{id}/seats")
  public List<SeatView> seats(@PathVariable UUID id) {
    return service.seats(id);
  }

  @GetMapping({"/admin/trips", "/counter/trips"})
  public PageResult<TripView> all(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.adminTrips(page, size);
  }

  @PostMapping("/admin/trips")
  @ResponseStatus(HttpStatus.CREATED)
  public TripView create(@Valid @RequestBody TripInput r) {
    return service.save(null, r);
  }

  @PatchMapping("/admin/trips/{id}")
  public TripView edit(@PathVariable UUID id, @Valid @RequestBody TripInput r) {
    return service.save(id, r);
  }

  @PostMapping("/admin/trips/{id}/publish")
  public TripView publish(@PathVariable UUID id) {
    return service.publish(id);
  }

  @PostMapping("/admin/trips/{id}/cancel")
  public Message cancel(@PathVariable UUID id, @Valid @RequestBody Reason r) {
    reservations.cancelTrip(id, r.reason());
    return new Message("Trip and reservations cancelled.");
  }

  @PatchMapping({"/admin/trips/{id}/status", "/driver/trips/{id}/status"})
  public TripView status(@PathVariable UUID id, @Valid @RequestBody Transition r) {
    return service.transition(id, r);
  }

  @GetMapping("/driver/trips")
  public List<TripView> assigned() {
    return service.assigned();
  }

  @GetMapping({
    "/admin/trips/{id}/manifest",
    "/driver/trips/{id}/manifest",
    "/counter/trips/{id}/manifest"
  })
  public List<BookingView> manifest(@PathVariable UUID id) {
    return reservations.manifest(id);
  }

  @PatchMapping("/admin/trips/{id}/fare")
  public TripView fare(@PathVariable UUID id, @Valid @RequestBody FareUpdate r) {
    return fares.fare(id, r);
  }
}
