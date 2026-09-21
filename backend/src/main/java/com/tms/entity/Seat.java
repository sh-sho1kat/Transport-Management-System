package com.tms.entity;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.*;

/** Persisted seat model. HTTP serialization is defined by a separate response DTO. */
// Seat collections are chosen per trip by SeatRepository.
public record Seat(
        @MongoId(FieldType.OBJECT_ID) String id,
        String seatNo,
        String bookingStatus,
        String studentId,
        String studentMail,
        Instant bookingDate,
        String bookingTime,
        @Field("__v") Integer version) {}
