package com.tms.service.impl;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.entity.*;
import com.tms.entity.Types.*;
import com.tms.exception.ApiException;
import com.tms.mapper.Views;
import com.tms.repository.*;
import com.tms.security.CurrentAccount;
import com.tms.service.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FleetService implements CatalogService {
  private final StopRepository stops;
  private final BusRepository buses;
  private final BusSeatRepository seats;
  private final RouteRepository routes;
  private final RouteStopRepository routeStops;
  private final TripRepository trips;
  private final CurrentAccount current;
  private final AuditService audit;

  public FleetService(
      StopRepository stops,
      BusRepository buses,
      BusSeatRepository seats,
      RouteRepository routes,
      RouteStopRepository routeStops,
      TripRepository trips,
      CurrentAccount current,
      AuditService audit) {
    this.stops = stops;
    this.buses = buses;
    this.seats = seats;
    this.routes = routes;
    this.routeStops = routeStops;
    this.trips = trips;
    this.current = current;
    this.audit = audit;
  }

  public List<StopView> stops(boolean all) {
    if (all) current.require(Role.ADMIN);
    return stops.findAll().stream()
        .filter(s -> all || s.getActive())
        .sorted(Comparator.comparing(Stop::getName))
        .map(Views::stop)
        .toList();
  }

  public StopView saveStop(UUID id, StopInput r) {
    Account actor = current.require(Role.ADMIN);
    Stop s = id == null ? new Stop() : stops.findById(id).orElseThrow(ApiException::missing);
    s.setName(r.name().trim());
    s.setCity(r.city().trim());
    s.setAddress(r.address().trim());
    s.setActive(r.active());
    stops.saveAndFlush(s);
    audit.record(actor, "STOP_SAVED", s.getId(), null);
    return Views.stop(s);
  }

  public List<BusView> buses() {
    current.require(Role.ADMIN);
    return buses.findAll().stream()
        .map(b -> Views.bus(b, seats.findByBusIdOrderByRowNumberAscColumnNumberAsc(b.getId())))
        .toList();
  }

  public BusView saveBus(UUID id, BusInput r) {
    Account actor = current.require(Role.ADMIN);
    Bus b = id == null ? new Bus() : buses.lockById(id).orElseThrow(ApiException::missing);
    if (id != null
        && trips.existsByBusIdAndStatusIn(id, List.of(TripStatus.PUBLISHED, TripStatus.DEPARTED)))
      throw ApiException.conflict(
          "BUS_IN_USE", "Finish or cancel active trips before changing this bus.");
    Set<String> labels = new HashSet<>(), positions = new HashSet<>();
    for (var s : r.seats())
      if (!labels.add(s.label()) || !positions.add(s.rowNumber() + ":" + s.columnNumber()))
        throw ApiException.invalid("Seat labels and positions must be unique.");
    b.setRegistration(r.registration().trim().toUpperCase(Locale.ROOT));
    b.setBusType(r.busType().trim());
    b.setActive(r.active());
    buses.saveAndFlush(b);
    if (id != null) {
      seats.deleteByBusId(id);
      seats.flush();
    }
    List<BusSeat> layout = new ArrayList<>();
    for (var s : r.seats()) {
      BusSeat v = new BusSeat();
      v.setBus(b);
      v.setLabel(s.label());
      v.setRowNumber(s.rowNumber());
      v.setColumnNumber(s.columnNumber());
      v.setBlocked(s.blocked());
      layout.add(seats.save(v));
    }
    audit.record(actor, "BUS_SAVED", b.getId(), null);
    return Views.bus(b, layout);
  }

  public List<RouteView> routes() {
    current.require(Role.ADMIN);
    return routes.findAll().stream()
        .map(r -> Views.route(r, routeStops.findByRouteIdOrderBySequence(r.getId())))
        .toList();
  }

  public RouteView saveRoute(UUID id, RouteInput r) {
    Account actor = current.require(Role.ADMIN);
    Route route = id == null ? new Route() : routes.lockById(id).orElseThrow(ApiException::missing);
    if (new HashSet<>(r.stopIds()).size() != r.stopIds().size())
      throw ApiException.invalid("A route cannot repeat a stop.");
    List<RouteStop> existing = id == null ? List.of() : routeStops.findByRouteIdOrderBySequence(id);
    boolean same = existing.stream().map(s -> s.getStop().getId()).toList().equals(r.stopIds());
    if (id != null && !same && trips.existsByRouteIdAndStatusNot(id, TripStatus.DRAFT))
      throw ApiException.conflict(
          "ROUTE_IN_USE",
          "Create a new route for a different itinerary; published trips keep their original"
              + " route.");
    route.setCode(r.code().trim().toUpperCase(Locale.ROOT));
    route.setName(r.name().trim());
    route.setActive(r.active());
    routes.saveAndFlush(route);
    if (!same) {
      if (id != null) {
        routeStops.deleteByRouteId(id);
        routeStops.flush();
      }
      existing = new ArrayList<>();
      for (int i = 0; i < r.stopIds().size(); i++) {
        Stop stop = stops.findById(r.stopIds().get(i)).orElseThrow(ApiException::missing);
        if (!stop.getActive()) throw ApiException.invalid("All route stops must be active.");
        RouteStop rs = new RouteStop();
        rs.setRoute(route);
        rs.setStop(stop);
        rs.setSequence(i);
        existing.add(routeStops.save(rs));
      }
    }
    audit.record(actor, "ROUTE_SAVED", route.getId(), null);
    return Views.route(route, existing);
  }
}
