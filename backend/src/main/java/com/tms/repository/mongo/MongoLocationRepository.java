package com.tms.repository.mongo;

import com.tms.entity.Location;
import com.tms.repository.LocationRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoLocationRepository extends AbstractMongoCrudRepository<Location> implements LocationRepository {
    public MongoLocationRepository(MongoTemplate mongo, LocationDocumentMapper mapper) {
        super(mongo, "locationtables", mapper);
    }
}
