package com.tms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationResponse(
        @JsonProperty("_id") String id,
        String location,
        @JsonProperty("__v") Integer version) {}
