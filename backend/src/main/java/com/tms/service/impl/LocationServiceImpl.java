package com.tms.service.impl;

import com.tms.dto.request.LocationRequest;
import com.tms.dto.response.*;
import com.tms.exception.ApiException;
import com.tms.mapper.LocationMapper;
import com.tms.repository.LocationRepository;
import com.tms.service.LocationService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {
    private final LocationRepository repository;
    private final LocationMapper mapper;
    public LocationServiceImpl(LocationRepository repository, LocationMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public CreateLocationResponse create(LocationRequest request) {
        return ServiceOperation.schedule(() -> new CreateLocationResponse("Location created successfully",
                mapper.toResponse(repository.create(mapper.toEntity(request)))));
    }
    public List<LocationResponse> findAll() {
        return ServiceOperation.schedule(() -> repository.findAll().stream().map(mapper::toResponse).toList());
    }
    public LocationResponse findById(String id) {
        return ServiceOperation.schedule(() -> mapper.toResponse(repository.findById(id).orElseThrow(this::notFound)));
    }
    public UpdateLocationResponse update(String id, LocationRequest request) {
        return ServiceOperation.schedule(() -> {
            var entity = mapper.toEntity(request); // Validate before looking up the ID, as in Node.
            return new UpdateLocationResponse("Location updated successfully",
                    mapper.toResponse(repository.update(id, entity).orElseThrow(this::notFound)));
        });
    }
    public MessageResponse delete(String id) {
        return ServiceOperation.schedule(() -> {
            if (!repository.deleteById(id)) throw notFound();
            return new MessageResponse("Location deleted successfully");
        });
    }
    private ApiException notFound() { return ApiException.message(404, "Location not found"); }
}
