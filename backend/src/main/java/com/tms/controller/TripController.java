package com.tms.controller;

import com.tms.dto.request.TripRequest;
import com.tms.dto.response.*;
import com.tms.service.TripService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class TripController {
    private final TripService service;
    public TripController(TripService service) { this.service = service; }

    @PostMapping({"/create-trip", "/create-trip/"})
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTripResponse create(@RequestBody(required = false) TripRequest request) {
        return service.create(request);
    }
    @GetMapping({"/get-trip", "/get-trip/"})
    public List<TripResponse> findAll() { return service.findAll(); }

    @GetMapping({"/get-trip/{id}", "/get-trip/{id}/"})
    public TripResponse findById(@PathVariable String id) { return service.findById(id); }

    @PutMapping({"/update-trip/{id}", "/update-trip/{id}/"})
    public UpdateTripResponse update(@PathVariable String id, @RequestBody(required = false) TripRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping({"/delete-trip/{id}", "/delete-trip/{id}/"})
    public MessageResponse delete(@PathVariable String id) { return service.delete(id); }
}
