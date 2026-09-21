package com.tms.repository.mongo;

import com.tms.entity.Trip;
import java.util.Date;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
class TripDocumentMapper implements DocumentMapper<Trip> {
    public Document fields(Trip entity) {
        return new Document()
                .append("busID", entity.busID())
                .append("tripID", entity.tripID())
                .append("startlocation", entity.startlocation())
                .append("destination", entity.destination())
                .append("date", entity.date() == null ? null : Date.from(entity.date()))
                .append("departuretime", entity.departuretime());
    }
    public Trip read(Document document) {
        return new Trip(document.getObjectId("_id").toHexString(), document.getString("busID"), document.getString("tripID"), document.getString("startlocation"), document.getString("destination"), document.getDate("date") == null ? null : document.getDate("date").toInstant(), document.getString("departuretime"), document.getInteger("__v"));
    }
}
