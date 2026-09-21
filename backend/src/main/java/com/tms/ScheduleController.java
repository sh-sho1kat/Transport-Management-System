package com.tms;

import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.tms.ScheduleService.Kind.*;

@RestController
@RequestMapping("/api/admin")
class ScheduleController {
    private final ScheduleService service;
    ScheduleController(ScheduleService service) { this.service = service; }
    private ResponseEntity<?> respond(int status, Supplier<Object> action) {
        try { return ResponseEntity.status(status).body(LegacyValues.json(action.get())); }
        catch (ApiException e) { throw e; }
        catch (RuntimeException e) { throw ApiException.message(500, "Server error"); }
    }

    @PostMapping({"/create-time", "/create-time/"})
    ResponseEntity<?> createTime(@RequestBody(required=false) Map<String,Object> body) {
        return respond(201, () -> service.create(TIME, body == null ? Map.of() : body));
    }
    @GetMapping({"/get-time", "/get-time/"})
    ResponseEntity<?> listTime() { return respond(200, () -> service.list(TIME)); }
    @GetMapping({"/get-time/{id}", "/get-time/{id}/"})
    ResponseEntity<?> getTime(@PathVariable String id) { return respond(200, () -> service.get(TIME, id)); }
    @PutMapping({"/update-time/{id}", "/update-time/{id}/"})
    ResponseEntity<?> updateTime(@PathVariable String id, @RequestBody(required=false) Map<String,Object> body) {
        return respond(200, () -> service.update(TIME, id, body == null ? Map.of() : body));
    }
    @DeleteMapping({"/delete-time/{id}", "/delete-time/{id}/"})
    ResponseEntity<?> deleteTime(@PathVariable String id) { return respond(200, () -> service.delete(TIME, id)); }

    @PostMapping({"/create-location", "/create-location/"})
    ResponseEntity<?> createLocation(@RequestBody(required=false) Map<String,Object> body) {
        return respond(201, () -> service.create(LOCATION, body == null ? Map.of() : body));
    }
    @GetMapping({"/get-location", "/get-location/"})
    ResponseEntity<?> listLocation() { return respond(200, () -> service.list(LOCATION)); }
    @GetMapping({"/get-location/{id}", "/get-location/{id}/"})
    ResponseEntity<?> getLocation(@PathVariable String id) { return respond(200, () -> service.get(LOCATION, id)); }
    @PutMapping({"/update-location/{id}", "/update-location/{id}/"})
    ResponseEntity<?> updateLocation(@PathVariable String id, @RequestBody(required=false) Map<String,Object> body) {
        return respond(200, () -> service.update(LOCATION, id, body == null ? Map.of() : body));
    }
    @DeleteMapping({"/delete-location/{id}", "/delete-location/{id}/"})
    ResponseEntity<?> deleteLocation(@PathVariable String id) { return respond(200, () -> service.delete(LOCATION, id)); }

    @PostMapping({"/create-trip", "/create-trip/"})
    ResponseEntity<?> createTrip(@RequestBody(required=false) Map<String,Object> body) {
        return respond(201, () -> service.create(TRIP, body == null ? Map.of() : body));
    }
    @GetMapping({"/get-trip", "/get-trip/"})
    ResponseEntity<?> listTrip() { return respond(200, () -> service.list(TRIP)); }
    @GetMapping({"/get-trip/{id}", "/get-trip/{id}/"})
    ResponseEntity<?> getTrip(@PathVariable String id) { return respond(200, () -> service.get(TRIP, id)); }
    @PutMapping({"/update-trip/{id}", "/update-trip/{id}/"})
    ResponseEntity<?> updateTrip(@PathVariable String id, @RequestBody(required=false) Map<String,Object> body) {
        return respond(200, () -> service.update(TRIP, id, body == null ? Map.of() : body));
    }
    @DeleteMapping({"/delete-trip/{id}", "/delete-trip/{id}/"})
    ResponseEntity<?> deleteTrip(@PathVariable String id) { return respond(200, () -> service.delete(TRIP, id)); }
}
