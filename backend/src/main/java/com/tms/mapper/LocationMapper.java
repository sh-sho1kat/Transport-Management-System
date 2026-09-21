package com.tms.mapper;

import com.tms.entity.Location;
import com.tms.dto.response.LocationResponse;
import com.tms.dto.request.LocationRequest;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public Location toEntity(LocationRequest request) {
        if (request == null) throw com.tms.exception.ApiException.message(400, "Location is required");
        RequestValues.require("Location is required", request.location());
        return new Location(null, RequestValues.string(request.location()), 0);
    }
    public LocationResponse toResponse(Location entity) {
        if (entity == null) return null;
        return new LocationResponse(entity.id(), entity.location(), entity.version());
    }
}
