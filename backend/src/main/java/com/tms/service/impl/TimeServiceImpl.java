package com.tms.service.impl;

import com.tms.dto.request.TimeRequest;
import com.tms.dto.response.*;
import com.tms.exception.ApiException;
import com.tms.mapper.TimeMapper;
import com.tms.repository.TimeRepository;
import com.tms.service.TimeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TimeServiceImpl implements TimeService {
    private final TimeRepository repository;
    private final TimeMapper mapper;
    public TimeServiceImpl(TimeRepository repository, TimeMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public CreateTimeResponse create(TimeRequest request) {
        return ServiceOperation.schedule(() -> new CreateTimeResponse("Time entry created successfully",
                mapper.toResponse(repository.create(mapper.toEntity(request)))));
    }
    public List<TimeResponse> findAll() {
        return ServiceOperation.schedule(() -> repository.findAll().stream().map(mapper::toResponse).toList());
    }
    public TimeResponse findById(String id) {
        return ServiceOperation.schedule(() -> mapper.toResponse(repository.findById(id).orElseThrow(this::notFound)));
    }
    public UpdateTimeResponse update(String id, TimeRequest request) {
        return ServiceOperation.schedule(() -> {
            var entity = mapper.toEntity(request); // Validate before looking up the ID, as in Node.
            return new UpdateTimeResponse("Time entry updated successfully",
                    mapper.toResponse(repository.update(id, entity).orElseThrow(this::notFound)));
        });
    }
    public MessageResponse delete(String id) {
        return ServiceOperation.schedule(() -> {
            if (!repository.deleteById(id)) throw notFound();
            return new MessageResponse("Time entry deleted successfully");
        });
    }
    private ApiException notFound() { return ApiException.message(404, "Time entry not found"); }
}
