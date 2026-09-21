package com.tms.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.*;
import com.tms.exception.ApiException;

/** Compatibility conversion at the HTTP boundary, without leaking raw JSON into repositories. */
public final class RequestValues {
    private RequestValues() {}
    public static boolean truthy(JsonNode value) {
        return value != null && !value.isNull() && !value.isMissingNode()
                && !(value.isBoolean() && !value.booleanValue())
                && !(value.isTextual() && value.textValue().isEmpty())
                && !(value.isNumber() && value.doubleValue() == 0);
    }
    public static void require(String message, JsonNode... values) {
        for (JsonNode value : values) if (!truthy(value)) throw ApiException.message(400, message);
    }
    public static String string(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) return null;
        if (value.isTextual() || value.isNumber() || value.isBoolean()) return value.asText();
        throw new IllegalArgumentException("Cannot cast value to String");
    }
    public static Instant instant(JsonNode value) {
        if (value.isNumber()) return Instant.ofEpochMilli(value.longValue());
        String s = string(value);
        try { return Instant.parse(s); } catch (RuntimeException ignored) { }
        try { return OffsetDateTime.parse(s).toInstant(); } catch (RuntimeException ignored) { }
        return LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
