package com.tms.repository.mongo;

import com.tms.entity.Seat;
import com.tms.entity.SeatChanges;
import com.tms.repository.SeatRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoSeatRepository implements SeatRepository {
    private final MongoTemplate mongo;
    private final SeatDocumentMapper mapper;
    public MongoSeatRepository(MongoTemplate mongo, SeatDocumentMapper mapper) { this.mongo = mongo; this.mapper = mapper; }
    private MongoCollection<Document> collection(String tripId) {
        return mongo.getCollection(MongooseCollectionNames.forTrip(tripId));
    }
    public void replaceAll(String tripId, List<Seat> seats) {
        var collection = collection(tripId);
        collection.deleteMany(new Document());
        collection.insertMany(seats.stream().map(seat -> mapper.fields(seat).append("_id", new ObjectId()).append("__v", 0)).toList());
    }
    public List<Seat> findAll(String tripId) { return find(tripId, new Document()); }
    public List<Seat> findBooked(String tripId) { return find(tripId, new Document("bookingStatus", "booked")); }
    public List<Seat> findByStudentId(String tripId, String studentId) { return find(tripId, new Document("studentId", studentId)); }
    private List<Seat> find(String tripId, Document filter) {
        return collection(tripId).find(filter).sort(new Document("seatNo", 1)).map(mapper::read).into(new ArrayList<>());
    }
    public Optional<Seat> findBySeatNo(String tripId, String seatNo) {
        return Optional.ofNullable(collection(tripId).find(new Document("seatNo", seatNo)).first()).map(mapper::read);
    }
    public Optional<Seat> update(String tripId, String seatNo, SeatChanges changes) {
        Document fields = new Document();
        if (changes.statusProvided()) fields.put("bookingStatus", changes.bookingStatus());
        if (changes.replaceBookingDetails()) {
            fields.append("studentId", changes.studentId()).append("studentMail", changes.studentMail())
                    .append("bookingDate", changes.bookingDate() == null ? null : Date.from(changes.bookingDate()))
                    .append("bookingTime", changes.bookingTime());
        }
        if (fields.isEmpty()) return findBySeatNo(tripId, seatNo);
        var document = collection(tripId).findOneAndUpdate(new Document("seatNo", seatNo), new Document("$set", fields),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        return Optional.ofNullable(document).map(mapper::read);
    }
}
