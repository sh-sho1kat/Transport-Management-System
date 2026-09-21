package com.tms.repository.mongo;

import com.tms.entity.Seat;
import java.util.Date;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
class SeatDocumentMapper implements DocumentMapper<Seat> {
    public Document fields(Seat entity) {
        return new Document()
                .append("seatNo", entity.seatNo())
                .append("bookingStatus", entity.bookingStatus())
                .append("studentId", entity.studentId())
                .append("studentMail", entity.studentMail())
                .append("bookingDate", entity.bookingDate() == null ? null : Date.from(entity.bookingDate()))
                .append("bookingTime", entity.bookingTime());
    }
    public Seat read(Document document) {
        return new Seat(document.getObjectId("_id").toHexString(), document.getString("seatNo"), document.getString("bookingStatus"), document.getString("studentId"), document.getString("studentMail"), document.getDate("bookingDate") == null ? null : document.getDate("bookingDate").toInstant(), document.getString("bookingTime"), document.getInteger("__v"));
    }
}
