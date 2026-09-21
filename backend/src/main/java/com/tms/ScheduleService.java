package com.tms;

import java.util.*;
import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
class ScheduleService {
    enum Kind {
        TIME("timetables", "Time entry", "Time", List.of("time")),
        LOCATION("locationtables", "Location", "Location", List.of("location")),
        TRIP("addtrips", "Trip", "Trip", List.of("busID", "tripID", "startlocation", "destination", "date", "departuretime"));
        final String collection, label, key;
        final List<String> fields;
        Kind(String collection, String label, String key, List<String> fields) {
            this.collection = collection; this.label = label; this.key = key; this.fields = fields;
        }
    }
    private final MongoStore store;
    ScheduleService(MongoStore store) { this.store = store; }
    private Document fields(Kind kind, Map<String,Object> body) {
        if (kind.fields.stream().anyMatch(f -> !LegacyValues.truthy(body.get(f)))) {
            throw ApiException.message(400, kind == Kind.TRIP ? "All fields are required" : kind.key + " is required");
        }
        Document fields = new Document();
        for (String field : kind.fields) {
            Object value = body.get(field);
            fields.put(field, field.equals("date") ? LegacyValues.date(value) : LegacyValues.string(value));
        }
        return fields;
    }
    Object create(Kind kind, Map<String,Object> body) {
        var saved = store.insert(kind.collection, fields(kind, body));
        String label = kind == Kind.TRIP ? "Trip entry" : kind.label;
        return Map.of("message", label + " created successfully", "new" + kind.key, saved);
    }
    Object list(Kind kind) { return store.list(kind.collection, new Document(), false); }
    Object get(Kind kind, String id) {
        return found(kind, store.one(kind.collection, store.id(id)));
    }
    Object update(Kind kind, String id, Map<String,Object> body) {
        Document fields = fields(kind, body);
        return Map.of("message", kind.label + " updated successfully", "updated" + kind.key,
                found(kind, store.update(kind.collection, store.id(id), fields)));
    }
    Object delete(Kind kind, String id) {
        found(kind, store.delete(kind.collection, store.id(id)));
        return Map.of("message", kind.label + " deleted successfully");
    }
    private Document found(Kind kind, Document value) {
        if (value == null) throw ApiException.message(404, kind.label + " not found");
        return value;
    }
}
