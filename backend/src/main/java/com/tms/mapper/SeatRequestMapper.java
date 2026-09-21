package com.tms.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.tms.dto.request.SeatUpdateRequest;
import com.tms.entity.SeatChanges;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SeatRequestMapper {
    public SeatUpdateRequest fromBulkElement(JsonNode node) {
        if (node == null || !node.isObject()) throw new IllegalArgumentException("Invalid seat data");
        return new SeatUpdateRequest(node.get("seatNo"), node.get("bookingStatus"), node.get("studentId"), node.get("studentMail"));
    }
    public boolean isBooked(SeatUpdateRequest request) {
        return request.bookingStatus() != null && request.bookingStatus().isTextual()
                && "booked".equals(request.bookingStatus().textValue());
    }
    public boolean needsStudent(SeatUpdateRequest request) {
        return isBooked(request) && (!RequestValues.truthy(request.studentId()) || !RequestValues.truthy(request.studentMail()));
    }
    public SeatChanges toChanges(SeatUpdateRequest request, Instant now, String time) {
        String status = RequestValues.string(request.bookingStatus());
        boolean booked = isBooked(request);
        boolean unbooked = request.bookingStatus() != null && request.bookingStatus().isTextual() && "unbooked".equals(status);
        return new SeatChanges(request.bookingStatus() != null, status, booked || unbooked,
                booked ? RequestValues.string(request.studentId()) : null,
                booked ? RequestValues.string(request.studentMail()) : null,
                booked ? now : null, booked ? time : null);
    }
}
