package com.tms.mapper;

import com.tms.entity.Time;
import com.tms.dto.response.TimeResponse;
import com.tms.dto.request.TimeRequest;
import org.springframework.stereotype.Component;

@Component
public class TimeMapper {
    public Time toEntity(TimeRequest request) {
        if (request == null) throw com.tms.exception.ApiException.message(400, "Time is required");
        RequestValues.require("Time is required", request.time());
        return new Time(null, RequestValues.string(request.time()), 0);
    }
    public TimeResponse toResponse(Time entity) {
        if (entity == null) return null;
        return new TimeResponse(entity.id(), entity.time(), entity.version());
    }
}
