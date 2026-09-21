package com.tms;

import com.tms.repository.mongo.MongooseCollectionNames;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionNamesTest {
    @ParameterizedTest
    @CsvSource({"TRIP00001,trip00001", "TRIP1,trip1", "TRIP, trips", "CITY,cities", "Bus,buses",
            "People,peoples", "PERSON,people", "fish,fish", "status,status", "knife,knives", "leaf,leafs", "wolf,wolves"})
    void usesMongooseCollectionNaming(String input, String expected) {
        assertThat(MongooseCollectionNames.forTrip(input)).isEqualTo(expected);
    }
}
