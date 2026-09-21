package com.tms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SeatResponse(
        @JsonProperty("_id") String id,
        String seatNo,
        String bookingStatus,
        String studentId,
        String studentMail,
        String bookingDate,
        String bookingTime,
        @JsonProperty("__v") Integer version) {}
