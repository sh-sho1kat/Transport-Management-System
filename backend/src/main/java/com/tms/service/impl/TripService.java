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
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TripService implements SchedulingService {
  private final TripRepository trips;
  private final RouteRepository routes;
  private final RouteStopRepository stops;
  private final BusRepository buses;
  private final BusSeatRepository layout;
  private final TripSeatRepository seats;
  private final AccountRepository accounts;
  private final CurrentAccount current;
  private final AuditService audit;
  private final Clock clock;
  private final ZoneId zone;
  private final int turnaround, cancellation;

  public TripService(
      TripRepository trips,
      RouteRepository routes,
      RouteStopRepository stops,
      BusRepository buses,
      BusSeatRepository layout,
      TripSeatRepository seats,
      AccountRepository accounts,
      CurrentAccount current,
      AuditService audit,
      Clock clock,
      @Value("${app.timezone}") String zone,
      @Value("${app.turnaround-minutes}") int turnaround,
      @Value("${app.cancel-hours}") int cancellation) {
    this.trips = trips;
    this.routes = routes;
    this.stops = stops;
    this.buses = buses;
    this.layout = layout;
    this.seats = seats;
    this.accounts = accounts;
    this.current = current;
    this.audit = audit;
    this.clock = clock;
    this.zone = ZoneId.of(zone);
    this.turnaround = turnaround;
    this.cancellation = cancellation;
  }

  public static Pageable paging(int page, int size, String field) {
    if (page < 0 || size < 1 || size > 100)
      throw ApiException.invalid("Page must be nonnegative and size between 1 and 100.");
    return PageRequest.of(page, size, Sort.by(field).and(Sort.by("id")));
  }

  public PageResult<TripView> search(
      UUID origin, UUID destination, LocalDate date, int page, int size) {
    Specification<Trip> spec =
        (root, q, cb) ->
            cb.and(
                cb.equal(root.get("status"), TripStatus.PUBLISHED),
                cb.greaterThan(root.get("salesCloseAt"), clock.instant()));
    if (date != null) {
      Instant start = date.atStartOfDay(zone).toInstant(),
          end = date.plusDays(1).atStartOfDay(zone).toInstant();
      spec =
          spec.and(
              (r, q, cb) ->
                  cb.and(
                      cb.greaterThanOrEqualTo(r.get("departureAt"), start),
                      cb.lessThan(r.get("departureAt"), end)));
    }
    if (origin != null || destination != null) {
      List<UUID> ids =
          routes.findAll().stream()
              .filter(
                  r -> {
                    var list = stops.findByRouteIdOrderBySequence(r.getId());
                    return !list.isEmpty()
                        && (origin == null || list.getFirst().getStop().getId().equals(origin))
                        && (destination == null
                            || list.getLast().getStop().getId().equals(destination));
                  })
              .map(Route::getId)
              .toList();
      spec = spec.and((r, q, cb) -> r.get("route").get("id").in(ids));
    }
    return Views.page(trips.findAll(spec, paging(page, size, "departureAt")), Views::trip);
  }

  private Trip visible(UUID id) {
    Trip t = trips.findById(id).orElseThrow(ApiException::missing);
    if (t.getStatus() != TripStatus.PUBLISHED) throw ApiException.missing();
    return t;
  }

  public TripView publicTrip(UUID id) {
    return Views.trip(visible(id));
  }

  public List<SeatView> seats(UUID id) {
    visible(id);
    Instant now = clock.instant();
    return seats.findByTripIdOrderByRowNumberAscColumnNumberAsc(id).stream()
        .map(
            s ->
                new SeatView(
                    s.getLabel(),
                    s.getRowNumber(),
                    s.getColumnNumber(),
                    s.getStatus() == SeatStatus.HELD && !s.getHold().getExpiresAt().isAfter(now)
                        ? SeatStatus.AVAILABLE
                        : s.getStatus()))
        .toList();
  }

  public PageResult<TripView> adminTrips(int page, int size) {
    var actor = current.get();
    if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.COUNTER_STAFF)
      throw new ApiException(403, "FORBIDDEN", "Staff access required.");
    return Views.page(trips.findAll(paging(page, size, "departureAt")), Views::trip);
  }

  public TripView save(UUID id, TripInput r) {
    Account actor = current.require(Role.ADMIN);
    Trip t = id == null ? new Trip() : trips.lockById(id).orElseThrow(ApiException::missing);
    if (id != null && t.getStatus() != TripStatus.DRAFT)
      throw ApiException.conflict("TRIP_IMMUTABLE", "Only draft trips can be edited.");
    if (!r.departureAt().isAfter(clock.instant())
        || !r.arrivalAt().isAfter(r.departureAt())
        || !r.salesCloseAt().isAfter(clock.instant())
        || r.salesCloseAt().isAfter(r.departureAt()))
      throw ApiException.invalid(
          "Use future sales closing and departure times, with arrival after departure.");
    try {
      Currency.getInstance(r.currency());
    } catch (IllegalArgumentException e) {
      throw ApiException.invalid("Currency must be a recognized ISO currency.");
    }
    Route route = routes.findById(r.routeId()).orElseThrow(ApiException::missing);
    Bus bus = buses.findById(r.busId()).orElseThrow(ApiException::missing);
    Account driver = accounts.findById(r.driverId()).orElseThrow(ApiException::missing);
    if (driver.getRole() != Role.DRIVER || !driver.getActive())
      throw ApiException.invalid("Assign an active driver.");
    var rs = stops.findByRouteIdOrderBySequence(route.getId());
    if (rs.size() < 2) throw ApiException.invalid("Route requires two stops.");
    t.setRoute(route);
    t.setBus(bus);
    t.setDriver(driver);
    t.setDepartureAt(r.departureAt());
    t.setArrivalAt(r.arrivalAt());
    t.setReservedUntil(r.arrivalAt().plusSeconds(turnaround * 60L));
    t.setSalesCloseAt(r.salesCloseAt());
    t.setFareMinor(r.fareMinor());
    t.setCurrency(r.currency());
    t.setStatus(TripStatus.DRAFT);
    t.setCancellationHours(cancellation);
    t.setOriginName(rs.getFirst().getStop().getName());
    t.setDestinationName(rs.getLast().getStop().getName());
    trips.saveAndFlush(t);
    audit.record(actor, "TRIP_DRAFT_SAVED", t.getId(), null);
    return Views.trip(t);
  }

  public TripView publish(UUID id) {
    Account actor = current.require(Role.ADMIN);
    Trip t = trips.lockById(id).orElseThrow(ApiException::missing);
    if (t.getStatus() != TripStatus.DRAFT)
      throw ApiException.conflict("INVALID_TRANSITION", "Only a draft can be published.");
    Route route = routes.lockById(t.getRoute().getId()).orElseThrow(ApiException::missing);
    Bus bus = buses.lockById(t.getBus().getId()).orElseThrow(ApiException::missing);
    var rs = stops.findByRouteIdOrderBySequence(route.getId());
    if (!route.getActive()
        || !bus.getActive()
        || !t.getDriver().getActive()
        || rs.stream().anyMatch(s -> !s.getStop().getActive()))
      throw ApiException.invalid("All assigned resources must be active.");
    if (!t.getSalesCloseAt().isAfter(clock.instant()))
      throw ApiException.invalid("Sales closing time has already passed.");
    var bs = layout.findByBusIdOrderByRowNumberAscColumnNumberAsc(bus.getId());
    if (bs.stream().noneMatch(s -> !s.getBlocked()))
      throw ApiException.invalid("Bus must have sellable seats.");
    t.setOriginName(rs.getFirst().getStop().getName());
    t.setDestinationName(rs.getLast().getStop().getName());
    t.setStatus(TripStatus.PUBLISHED);
    trips.saveAndFlush(t);
    for (BusSeat s : bs) {
      TripSeat v = new TripSeat();
      v.setTrip(t);
      v.setLabel(s.getLabel());
      v.setRowNumber(s.getRowNumber());
      v.setColumnNumber(s.getColumnNumber());
      v.setStatus(s.getBlocked() ? SeatStatus.BLOCKED : SeatStatus.AVAILABLE);
      seats.save(v);
    }
    audit.record(actor, "TRIP_PUBLISHED", id, null);
    return Views.trip(t);
  }

  public List<TripView> assigned() {
    Account a = current.require(Role.DRIVER);
    return trips.findByDriverIdOrderByDepartureAt(a.getId()).stream()
        .filter(t -> t.getStatus() != TripStatus.DRAFT)
        .map(Views::trip)
        .toList();
  }

  public TripView transition(UUID id, Transition r) {
    Account actor = current.get();
    Trip t = trips.lockById(id).orElseThrow(ApiException::missing);
    if (actor.getRole() != Role.ADMIN
        && (actor.getRole() != Role.DRIVER || !actor.getId().equals(t.getDriver().getId())))
      throw ApiException.missing();
    boolean valid =
        t.getStatus() == TripStatus.PUBLISHED
                && r.status() == TripStatus.DEPARTED
                && !clock.instant().isBefore(t.getDepartureAt())
            || t.getStatus() == TripStatus.DEPARTED && r.status() == TripStatus.COMPLETED;
    if (!valid)
      throw ApiException.conflict(
          "INVALID_TRANSITION",
          "Departure requires the scheduled time; completion requires a departed trip.");
    t.setStatus(r.status());
    audit.record(actor, "TRIP_" + r.status(), id, null);
    return Views.trip(t);
  }
}
