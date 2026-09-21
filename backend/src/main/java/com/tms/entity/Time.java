package com.tms.entity;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.*;

/** Persisted time model. HTTP serialization is defined by a separate response DTO. */
@Document("timetables")
public record Time(
        @MongoId(FieldType.OBJECT_ID) String id,
        String time,
        @Field("__v") Integer version) {}
