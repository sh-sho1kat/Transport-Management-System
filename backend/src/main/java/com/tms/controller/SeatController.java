package com.tms.controller;

import com.tms.dto.request.*;
import com.tms.dto.response.*;
import com.tms.service.SeatService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/seats")
public class SeatController {
    private final SeatService service;
    public SeatController(SeatService service) { this.service = service; }
    @PostMapping({"/create/{tripId}", "/create/{tripId}/"})
    @ResponseStatus(HttpStatus.CREATED)
    public SeatInitializationResponse initialize(@PathVariable String tripId) { return service.initialize(tripId); }

    @GetMapping({"/{tripId}", "/{tripId}/"})
    public List<SeatResponse> findAll(@PathVariable String tripId) { return service.findAll(tripId); }

    @GetMapping({"/{tripId}/booked", "/{tripId}/booked/"})
    public List<SeatResponse> findBooked(@PathVariable String tripId) { return service.findBooked(tripId); }

    @GetMapping({"/{tripId}/student/{studentId}", "/{tripId}/student/{studentId}/"})
    public List<SeatResponse> findByStudentId(@PathVariable String tripId, @PathVariable String studentId) {
        return service.findByStudentId(tripId, studentId);
    }
    @GetMapping({"/{tripId}/{seatNo}", "/{tripId}/{seatNo}/"})
    public SeatResponse findBySeatNo(@PathVariable String tripId, @PathVariable String seatNo) {
        return service.findBySeatNo(tripId, seatNo);
    }
    @PutMapping({"/{tripId}/{seatNo}", "/{tripId}/{seatNo}/"})
    public SeatUpdateResponse update(@PathVariable String tripId, @PathVariable String seatNo,
                                     @RequestBody(required = false) SeatUpdateRequest request) {
        return service.update(tripId, seatNo, request);
    }
    @PutMapping({"/{tripId}", "/{tripId}/"})
    public BulkSeatUpdateResponse updateMany(@PathVariable String tripId, @RequestBody(required = false) BulkSeatUpdateRequest request) {
        return service.updateMany(tripId, request);
    }
}
