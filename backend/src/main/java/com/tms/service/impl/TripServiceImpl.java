package com.tms.service.impl;

import com.tms.dto.request.TripRequest;
import com.tms.dto.response.*;
import com.tms.exception.ApiException;
import com.tms.mapper.TripMapper;
import com.tms.repository.TripRepository;
import com.tms.service.TripService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TripServiceImpl implements TripService {
    private final TripRepository repository;
    private final TripMapper mapper;
    public TripServiceImpl(TripRepository repository, TripMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public CreateTripResponse create(TripRequest request) {
        return ServiceOperation.schedule(() -> new CreateTripResponse("Trip entry created successfully",
                mapper.toResponse(repository.create(mapper.toEntity(request)))));
    }
    public List<TripResponse> findAll() {
        return ServiceOperation.schedule(() -> repository.findAll().stream().map(mapper::toResponse).toList());
    }
    public TripResponse findById(String id) {
        return ServiceOperation.schedule(() -> mapper.toResponse(repository.findById(id).orElseThrow(this::notFound)));
    }
    public UpdateTripResponse update(String id, TripRequest request) {
        return ServiceOperation.schedule(() -> {
            var entity = mapper.toEntity(request); // Validate before looking up the ID, as in Node.
            return new UpdateTripResponse("Trip updated successfully",
                    mapper.toResponse(repository.update(id, entity).orElseThrow(this::notFound)));
        });
    }
    public MessageResponse delete(String id) {
        return ServiceOperation.schedule(() -> {
            if (!repository.deleteById(id)) throw notFound();
            return new MessageResponse("Trip deleted successfully");
        });
    }
    private ApiException notFound() { return ApiException.message(404, "Trip not found"); }
}
