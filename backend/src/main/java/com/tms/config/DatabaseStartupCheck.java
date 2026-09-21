package com.tms.config;

import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Like the Node bootstrap, fail startup if MongoDB cannot be reached. */
@Configuration
class DatabaseStartupCheck {
    @Bean InitializingBean verifyMongoConnection(MongoTemplate mongo) {
        return () -> mongo.getDb().runCommand(new Document("ping", 1));
    }
}
