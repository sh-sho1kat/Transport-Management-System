package com.tms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/** Raw field nodes preserve the original API's coercion and validation order; mapped before persistence. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeatUpdateRequest(JsonNode seatNo, JsonNode bookingStatus, JsonNode studentId, JsonNode studentMail) {}
