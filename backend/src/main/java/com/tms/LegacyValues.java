package com.tms;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import org.bson.types.ObjectId;

/** Boundary conversions keep Mongoose string casting, BSON dates and JSON wire types explicit. */
final class LegacyValues {
    private LegacyValues() {}
    private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

    static boolean truthy(Object value) {
        return value != null && !Boolean.FALSE.equals(value)
                && !(value instanceof String s && s.isEmpty())
                && !(value instanceof Number n && n.doubleValue() == 0);
    }
    static String string(Object value) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return value.toString();
        throw new IllegalArgumentException("Cannot cast value to String");
    }
    static Date date(Object value) {
        if (value instanceof Number n) return new Date(n.longValue());
        String s = string(value);
        try { return Date.from(Instant.parse(s)); } catch (RuntimeException ignored) { }
        try { return Date.from(OffsetDateTime.parse(s).toInstant()); } catch (RuntimeException ignored) { }
        return Date.from(LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant());
    }
    static Object json(Object value) {
        if (value instanceof ObjectId id) return id.toHexString();
        if (value instanceof Date date) return ISO_MILLIS.format(date.toInstant());
        if (value instanceof Map<?, ?> map) {
            Map<String,Object> result = new LinkedHashMap<>();
            map.forEach((k,v) -> result.put(k.toString(), json(v)));
            return result;
        }
        if (value instanceof List<?> list) return list.stream().map(LegacyValues::json).toList();
        return value;
    }
}
