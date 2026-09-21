package com.tms.repository.mongo;

import com.tms.entity.Trip;
import com.tms.repository.TripRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoTripRepository extends AbstractMongoCrudRepository<Trip> implements TripRepository {
    public MongoTripRepository(MongoTemplate mongo, TripDocumentMapper mapper) {
        super(mongo, "addtrips", mapper);
    }
}
