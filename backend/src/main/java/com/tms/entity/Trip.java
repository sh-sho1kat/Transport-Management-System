package com.tms.entity;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.*;

/** Persisted trip model. HTTP serialization is defined by a separate response DTO. */
@Document("addtrips")
public record Trip(
        @MongoId(FieldType.OBJECT_ID) String id,
        String busID,
        String tripID,
        String startlocation,
        String destination,
        Instant date,
        String departuretime,
        @Field("__v") Integer version) {}
