package com.tms.dto.response;

public record BulkSeatUpdateResponse(String message, java.util.List<SeatResponse> seats, String tripId) {}
