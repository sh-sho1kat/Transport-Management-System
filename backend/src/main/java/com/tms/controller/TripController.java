package com.tms.controller;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.service.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TripController {
  private final SchedulingService service;
  private final ReservationService reservations;

  public TripController(SchedulingService service, ReservationService reservations) {
    this.service = service;
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

  @GetMapping("/admin/trips")
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

  @GetMapping({"/admin/trips/{id}/manifest", "/driver/trips/{id}/manifest"})
  public List<BookingView> manifest(@PathVariable UUID id) {
    return reservations.manifest(id);
  }
}
