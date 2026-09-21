package com.tms.entity;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.*;

/** Persisted location model. HTTP serialization is defined by a separate response DTO. */
@Document("locationtables")
public record Location(
        @MongoId(FieldType.OBJECT_ID) String id,
        String location,
        @Field("__v") Integer version) {}
