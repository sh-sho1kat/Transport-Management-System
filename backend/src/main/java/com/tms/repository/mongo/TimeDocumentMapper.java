package com.tms.repository.mongo;

import com.tms.entity.Time;
import java.util.Date;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
class TimeDocumentMapper implements DocumentMapper<Time> {
    public Document fields(Time entity) {
        return new Document()
                .append("time", entity.time());
    }
    public Time read(Document document) {
        return new Time(document.getObjectId("_id").toHexString(), document.getString("time"), document.getInteger("__v"));
    }
}
