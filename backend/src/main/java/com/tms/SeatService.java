package com.tms;

import java.time.*;
import java.time.format.*;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class SeatService {
    private final MongoStore store;
    private final ZoneId zone;
    private final DateTimeFormatter timeFormat;
    SeatService(MongoStore store, @Value("${tms.booking-zone}") String zone,
                @Value("${tms.booking-locale}") String locale) {
        this.store = store;
        this.zone = ZoneId.of(zone);
        this.timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(Locale.forLanguageTag(locale));
    }
    Object create(String tripId) {
        List<Document> seats = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            seats.add(new Document("_id", new ObjectId()).append("seatNo", String.format(Locale.ROOT, "%02d", i))
                    .append("bookingStatus", "unbooked").append("studentId", null).append("studentMail", null)
                    .append("bookingDate", null).append("bookingTime", null).append("__v", 0));
        }
        store.resetSeats(MongooseCollectionNames.forTrip(tripId), seats);
        return Map.of("message", "Seats created successfully for trip " + tripId + "!", "tripId", tripId);
    }
    Object list(String tripId) { return list(tripId, new Document()); }
    Object booked(String tripId) { return list(tripId, new Document("bookingStatus", "booked")); }
    Object student(String tripId, String studentId) {
        var seats = list(tripId, new Document("studentId", studentId));
        if (seats.isEmpty()) throw ApiException.message(404, "No bookings found for this student in this trip");
        return seats;
    }
    private List<Document> list(String tripId, Document filter) {
        return store.list(MongooseCollectionNames.forTrip(tripId), filter, true);
    }
    Object get(String tripId, String seatNo) {
        return found(store.one(MongooseCollectionNames.forTrip(tripId), new Document("seatNo", seatNo)));
    }
    Object update(String tripId, String seatNo, Map<String,Object> body) {
        if (!LegacyValues.truthy(body.get("bookingStatus"))) throw ApiException.message(400, "Booking status is required");
        if (needsStudent(body)) throw ApiException.message(400, "Student details are required for booking");
        Document result = found(updateOne(tripId, seatNo, body, new Date()));
        return Map.of("message", "Seat updated successfully", "seat", result);
    }
    Object updateMany(String tripId, Map<String,Object> body) {
        if (!(body.get("seats") instanceof List<?> seats) || seats.isEmpty()) {
            throw ApiException.message(400, "Invalid seats data");
        }
        Date now = new Date();
        List<Document> results = new ArrayList<>();
        RuntimeException firstFailure = null;
        // The original Promise.all is not transactional: valid entries may persist even when another fails.
        for (Object value : seats) {
            try {
                if (!(value instanceof Map<?,?>)) throw new IllegalArgumentException("Invalid seat data");
                @SuppressWarnings("unchecked") Map<String,Object> seat = (Map<String,Object>) value;
                if (needsStudent(seat)) throw new IllegalArgumentException("Student details required for seat "
                        + (seat.containsKey("seatNo") ? seat.get("seatNo") : "undefined"));
                results.add(updateOne(tripId, seat.get("seatNo"), seat, now));
            } catch (RuntimeException e) {
                if (firstFailure == null) firstFailure = e;
            }
        }
        if (firstFailure != null) throw firstFailure;
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("message", "Seats updated successfully");
        response.put("seats", results); // Missing seats remain null, matching Promise.all's result.
        response.put("tripId", tripId);
        return response;
    }
    private boolean needsStudent(Map<String,Object> body) {
        return "booked".equals(body.get("bookingStatus"))
                && (!LegacyValues.truthy(body.get("studentId")) || !LegacyValues.truthy(body.get("studentMail")));
    }
    private Document updateOne(String tripId, Object seatNo, Map<String,Object> body, Date now) {
        Object status = body.get("bookingStatus");
        Document fields = new Document();
        // Mongoose findOneAndUpdate does not run enum validators in this source; preserve that behavior.
        if (body.containsKey("bookingStatus")) fields.put("bookingStatus", LegacyValues.string(status));
        if ("booked".equals(status)) {
            fields.append("studentId", LegacyValues.string(body.get("studentId")))
                    .append("studentMail", LegacyValues.string(body.get("studentMail")))
                    .append("bookingDate", now)
                    .append("bookingTime", timeFormat.format(now.toInstant().atZone(zone)).replace('\u202f', ' '));
        } else if ("unbooked".equals(status)) {
            fields.append("studentId", null).append("studentMail", null).append("bookingDate", null).append("bookingTime", null);
        }
        return store.update(MongooseCollectionNames.forTrip(tripId), new Document("seatNo", LegacyValues.string(seatNo)), fields);
    }
    private Document found(Document seat) {
        if (seat == null) throw ApiException.message(404, "Seat not found");
        return seat;
    }
}
