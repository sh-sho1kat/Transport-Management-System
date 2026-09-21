package com.tms.mapper;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

final class ResponseDates {
    private static final DateTimeFormatter ISO = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
    private ResponseDates() {}
    static String format(Instant instant) { return instant == null ? null : ISO.format(instant); }
}
