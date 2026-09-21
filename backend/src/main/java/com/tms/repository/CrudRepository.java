package com.tms.repository;

import java.util.List;
import java.util.Optional;

/** Application-owned persistence contract; the Mongo adapter implements atomic updates. */
public interface CrudRepository<T> {
    T create(T entity);
    List<T> findAll();
    Optional<T> findById(String id);
    Optional<T> update(String id, T entity);
    boolean deleteById(String id);
}
