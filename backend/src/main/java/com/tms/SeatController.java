package com.tms;

import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/seats")
class SeatController {
    private final SeatService service;
    SeatController(SeatService service) { this.service = service; }
    private ResponseEntity<?> respond(int status, String failure, Supplier<Object> action) {
        try { return ResponseEntity.status(status).body(LegacyValues.json(action.get())); }
        catch (ApiException e) { throw e; }
        catch (RuntimeException e) { throw new ApiException(500, "error", failure); }
    }
    @PostMapping({"/create/{tripId}", "/create/{tripId}/"})
    ResponseEntity<?> create(@PathVariable String tripId) {
        return respond(201, "Failed to create seats.", () -> service.create(tripId));
    }
    @GetMapping({"/{tripId}", "/{tripId}/"})
    ResponseEntity<?> list(@PathVariable String tripId) {
        return respond(200, "Failed to fetch seats.", () -> service.list(tripId));
    }
    @GetMapping({"/{tripId}/booked", "/{tripId}/booked/"})
    ResponseEntity<?> booked(@PathVariable String tripId) {
        return respond(200, "Failed to fetch booked seats.", () -> service.booked(tripId));
    }
    @GetMapping({"/{tripId}/student/{studentId}", "/{tripId}/student/{studentId}/"})
    ResponseEntity<?> student(@PathVariable String tripId, @PathVariable String studentId) {
        return respond(200, "Failed to fetch student bookings.", () -> service.student(tripId, studentId));
    }
    @GetMapping({"/{tripId}/{seatNo}", "/{tripId}/{seatNo}/"})
    ResponseEntity<?> get(@PathVariable String tripId, @PathVariable String seatNo) {
        return respond(200, "Failed to fetch seat.", () -> service.get(tripId, seatNo));
    }
    @PutMapping({"/{tripId}/{seatNo}", "/{tripId}/{seatNo}/"})
    ResponseEntity<?> update(@PathVariable String tripId, @PathVariable String seatNo,
                             @RequestBody(required=false) Map<String,Object> body) {
        return respond(200, "Failed to update seat.", () -> service.update(tripId, seatNo, body == null ? Map.of() : body));
    }
    @PutMapping({"/{tripId}", "/{tripId}/"})
    ResponseEntity<?> updateMany(@PathVariable String tripId, @RequestBody(required=false) Map<String,Object> body) {
        try {
            return ResponseEntity.ok(LegacyValues.json(service.updateMany(tripId, body == null ? Map.of() : body)));
        } catch (ApiException e) { throw e; }
        catch (RuntimeException e) {
            throw new ApiException(500, "error", e.getMessage() == null ? "Failed to update seats." : e.getMessage());
        }
    }
}
