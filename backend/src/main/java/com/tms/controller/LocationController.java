package com.tms.controller;

import com.tms.dto.request.LocationRequest;
import com.tms.dto.response.*;
import com.tms.service.LocationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class LocationController {
    private final LocationService service;
    public LocationController(LocationService service) { this.service = service; }

    @PostMapping({"/create-location", "/create-location/"})
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLocationResponse create(@RequestBody(required = false) LocationRequest request) {
        return service.create(request);
    }
    @GetMapping({"/get-location", "/get-location/"})
    public List<LocationResponse> findAll() { return service.findAll(); }

    @GetMapping({"/get-location/{id}", "/get-location/{id}/"})
    public LocationResponse findById(@PathVariable String id) { return service.findById(id); }

    @PutMapping({"/update-location/{id}", "/update-location/{id}/"})
    public UpdateLocationResponse update(@PathVariable String id, @RequestBody(required = false) LocationRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping({"/delete-location/{id}", "/delete-location/{id}/"})
    public MessageResponse delete(@PathVariable String id) { return service.delete(id); }
}
