package com.tms.repository.mongo;

import com.tms.repository.CrudRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

abstract class AbstractMongoCrudRepository<T> implements CrudRepository<T> {
    private final MongoTemplate mongo;
    private final String name;
    private final DocumentMapper<T> mapper;
    protected AbstractMongoCrudRepository(MongoTemplate mongo, String name, DocumentMapper<T> mapper) {
        this.mongo = mongo; this.name = name; this.mapper = mapper;
    }
    private MongoCollection<Document> collection() { return mongo.getCollection(name); }
    private Document id(String id) { return new Document("_id", new ObjectId(id)); }
    public T create(T entity) {
        Document document = mapper.fields(entity).append("_id", new ObjectId()).append("__v", 0);
        collection().insertOne(document);
        return mapper.read(document);
    }
    public List<T> findAll() { return collection().find().map(mapper::read).into(new ArrayList<>()); }
    public Optional<T> findById(String id) { return Optional.ofNullable(collection().find(id(id)).first()).map(mapper::read); }
    public Optional<T> update(String id, T entity) {
        Document updated = collection().findOneAndUpdate(id(id), new Document("$set", mapper.fields(entity)),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        return Optional.ofNullable(updated).map(mapper::read);
    }
    public boolean deleteById(String id) { return collection().findOneAndDelete(id(id)) != null; }
}
