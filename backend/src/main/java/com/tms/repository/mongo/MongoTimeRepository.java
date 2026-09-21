package com.tms.repository.mongo;

import com.tms.entity.Time;
import com.tms.repository.TimeRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoTimeRepository extends AbstractMongoCrudRepository<Time> implements TimeRepository {
    public MongoTimeRepository(MongoTemplate mongo, TimeDocumentMapper mapper) {
        super(mongo, "timetables", mapper);
    }
}
