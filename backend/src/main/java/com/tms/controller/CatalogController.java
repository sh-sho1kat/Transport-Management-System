package com.tms.controller;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.service.CatalogService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
  private final CatalogService service;

  public CatalogController(CatalogService service) {
    this.service = service;
  }

  @GetMapping("/stops")
  public List<StopView> publicStops() {
    return service.stops(false);
  }

  @GetMapping("/admin/stops")
  public List<StopView> stops() {
    return service.stops(true);
  }

  @PostMapping("/admin/stops")
  @ResponseStatus(HttpStatus.CREATED)
  public StopView stop(@Valid @RequestBody StopInput r) {
    return service.saveStop(null, r);
  }

  @PatchMapping("/admin/stops/{id}")
  public StopView stop(@PathVariable UUID id, @Valid @RequestBody StopInput r) {
    return service.saveStop(id, r);
  }

  @GetMapping("/admin/buses")
  public List<BusView> buses() {
    return service.buses();
  }

  @PostMapping("/admin/buses")
  @ResponseStatus(HttpStatus.CREATED)
  public BusView bus(@Valid @RequestBody BusInput r) {
    return service.saveBus(null, r);
  }

  @PatchMapping("/admin/buses/{id}")
  public BusView bus(@PathVariable UUID id, @Valid @RequestBody BusInput r) {
    return service.saveBus(id, r);
  }

  @GetMapping("/admin/routes")
  public List<RouteView> routes() {
    return service.routes();
  }

  @PostMapping("/admin/routes")
  @ResponseStatus(HttpStatus.CREATED)
  public RouteView route(@Valid @RequestBody RouteInput r) {
    return service.saveRoute(null, r);
  }

  @PatchMapping("/admin/routes/{id}")
  public RouteView route(@PathVariable UUID id, @Valid @RequestBody RouteInput r) {
    return service.saveRoute(id, r);
  }
}
