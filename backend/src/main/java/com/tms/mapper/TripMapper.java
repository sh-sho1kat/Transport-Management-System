package com.tms.mapper;

import com.tms.entity.Trip;
import com.tms.dto.response.TripResponse;
import com.tms.dto.request.TripRequest;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {
    public Trip toEntity(TripRequest request) {
        if (request == null) throw com.tms.exception.ApiException.message(400, "All fields are required");
        RequestValues.require("All fields are required", request.busID(), request.tripID(), request.startlocation(), request.destination(), request.date(), request.departuretime());
        return new Trip(null, RequestValues.string(request.busID()), RequestValues.string(request.tripID()), RequestValues.string(request.startlocation()), RequestValues.string(request.destination()), RequestValues.instant(request.date()), RequestValues.string(request.departuretime()), 0);
    }
    public TripResponse toResponse(Trip entity) {
        if (entity == null) return null;
        return new TripResponse(entity.id(), entity.busID(), entity.tripID(), entity.startlocation(), entity.destination(), ResponseDates.format(entity.date()), entity.departuretime(), entity.version());
    }
}
