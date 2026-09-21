package com.tms.repository.mongo;

import org.bson.Document;

interface DocumentMapper<T> {
    Document fields(T entity);
    T read(Document document);
}
