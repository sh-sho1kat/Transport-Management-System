package com.tms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TripResponse(
        @JsonProperty("_id") String id,
        String busID,
        String tripID,
        String startlocation,
        String destination,
        String date,
        String departuretime,
        @JsonProperty("__v") Integer version) {}
