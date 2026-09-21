package com.tms.controller;

import com.tms.dto.request.TimeRequest;
import com.tms.dto.response.*;
import com.tms.service.TimeService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class TimeController {
    private final TimeService service;
    public TimeController(TimeService service) { this.service = service; }

    @PostMapping({"/create-time", "/create-time/"})
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTimeResponse create(@RequestBody(required = false) TimeRequest request) {
        return service.create(request);
    }
    @GetMapping({"/get-time", "/get-time/"})
    public List<TimeResponse> findAll() { return service.findAll(); }

    @GetMapping({"/get-time/{id}", "/get-time/{id}/"})
    public TimeResponse findById(@PathVariable String id) { return service.findById(id); }

    @PutMapping({"/update-time/{id}", "/update-time/{id}/"})
    public UpdateTimeResponse update(@PathVariable String id, @RequestBody(required = false) TimeRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping({"/delete-time/{id}", "/delete-time/{id}/"})
    public MessageResponse delete(@PathVariable String id) { return service.delete(id); }
}
