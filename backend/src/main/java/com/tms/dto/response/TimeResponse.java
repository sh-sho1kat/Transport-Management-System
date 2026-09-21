package com.tms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimeResponse(
        @JsonProperty("_id") String id,
        String time,
        @JsonProperty("__v") Integer version) {}
