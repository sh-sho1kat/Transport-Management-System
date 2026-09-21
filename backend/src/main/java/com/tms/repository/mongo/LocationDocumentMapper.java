package com.tms.repository.mongo;

import com.tms.entity.Location;
import java.util.Date;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
class LocationDocumentMapper implements DocumentMapper<Location> {
    public Document fields(Location entity) {
        return new Document()
                .append("location", entity.location());
    }
    public Location read(Document document) {
        return new Location(document.getObjectId("_id").toHexString(), document.getString("location"), document.getInteger("__v"));
    }
}
