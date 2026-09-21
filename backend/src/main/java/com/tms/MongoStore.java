package com.tms;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/** Raw BSON avoids Spring's _class field and preserves the existing database representation. */
@Repository
class MongoStore {
    private final MongoTemplate mongo;
    MongoStore(MongoTemplate mongo) { this.mongo = mongo; }
    MongoCollection<Document> collection(String name) { return mongo.getCollection(name); }
    Document id(String id) { return new Document("_id", new ObjectId(id)); }
    Document insert(String name, Document value) {
        value.putIfAbsent("_id", new ObjectId());
        value.putIfAbsent("__v", 0);
        collection(name).insertOne(value);
        return value;
    }
    List<Document> list(String name, Document filter, boolean sortedSeats) {
        var query = collection(name).find(filter);
        if (sortedSeats) query.sort(new Document("seatNo", 1));
        return query.into(new ArrayList<>());
    }
    Document one(String name, Document filter) { return collection(name).find(filter).first(); }
    Document update(String name, Document filter, Document fields) {
        if (fields.isEmpty()) return one(name, filter);
        return collection(name).findOneAndUpdate(filter, new Document("$set", fields),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
    }
    Document delete(String name, Document filter) { return collection(name).findOneAndDelete(filter); }
    void resetSeats(String name, List<Document> seats) {
        collection(name).deleteMany(new Document());
        collection(name).insertMany(seats);
    }
}
