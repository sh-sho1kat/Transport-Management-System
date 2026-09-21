package com.tms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiCompatibilityTest {
    static final MongoServer SERVER = new MongoServer(new MemoryBackend());
    static final InetSocketAddress ADDRESS = SERVER.bind();
    @DynamicPropertySource
    static void mongo(DynamicPropertyRegistry properties) {
        properties.add("spring.data.mongodb.uri", () -> "mongodb://localhost:" + ADDRESS.getPort() + "/tms_test");
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired MongoTemplate mongo;
    private static final String BASE = "/api/admin";
    private static final String TRIP = "TRIP12345";
    private static final String SEATS = BASE + "/seats/" + TRIP;
    @BeforeEach void clearDatabase() { mongo.getDb().drop(); }
    @AfterAll static void stopMongo() { SERVER.shutdownNow(); }
    JsonNode body(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsString()); }
    String payload(String kind) {
        return switch (kind) {
            case "time" -> "{\"time\":\"08:00\",\"ignored\":true}";
            case "location" -> "{\"location\":\"Campus\",\"ignored\":true}";
            default -> """
                {"busID":"BUS01","tripID":"TRIP12345","startlocation":"Campus","destination":"City",
                 "date":"2026-09-21T08:00:00.000Z","departuretime":"08:00","ignored":true}
                """;
        };
    }
    void initialize() throws Exception {
        mvc.perform(post(BASE + "/seats/create/" + TRIP)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.tripId").value(TRIP))
                .andExpect(jsonPath("$.message").value("Seats created successfully for trip " + TRIP + "!"));
    }
    @ParameterizedTest @ValueSource(strings={"time","location","trip"})
    void fullScheduleLifecycle(String kind) throws Exception {
        String key = kind.substring(0,1).toUpperCase() + kind.substring(1);
        String label = kind.equals("time") ? "Time entry" : key;
        JsonNode created = body(mvc.perform(post(BASE + "/create-" + kind).contentType("application/json").content(payload(kind)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value((kind.equals("trip") ? "Trip entry" : label) + " created successfully"))
                .andReturn()).get("new" + key);
        String id = created.get("_id").asText();
        assertThat(id).matches("[0-9a-f]{24}");
        assertThat(created.get("__v").asInt()).isZero();
        assertThat(created.has("ignored")).isFalse();
        assertThat(created.has("_class")).isFalse();
        if (kind.equals("trip")) assertThat(created.get("date").asText()).isEqualTo("2026-09-21T08:00:00.000Z");
        assertThat(body(mvc.perform(get(BASE+"/get-"+kind+"/"+id)).andExpect(status().isOk()).andReturn())).isEqualTo(created);
        mvc.perform(get(BASE+"/get-"+kind)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put(BASE+"/update-"+kind+"/"+id).contentType("application/json").content(payload(kind)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value(label+" updated successfully"))
                .andExpect(jsonPath("$.updated"+key+"._id").value(id));
        mvc.perform(delete(BASE+"/delete-"+kind+"/"+id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(label+" deleted successfully"));
        mvc.perform(get(BASE+"/get-"+kind+"/"+id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(label+" not found"));
        mvc.perform(get(BASE+"/get-"+kind)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @ParameterizedTest @ValueSource(strings={"time","location","trip"})
    void validationAndMissingIds(String kind) throws Exception {
        String key = kind.substring(0,1).toUpperCase() + kind.substring(1);
        String error = kind.equals("trip") ? "All fields are required" : key+" is required";
        mvc.perform(post(BASE+"/create-"+kind).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(error));
        mvc.perform(put(BASE+"/update-"+kind+"/bad-id").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(error));
        mvc.perform(get(BASE+"/get-"+kind+"/bad-id")).andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"Server error\"}"));
        String missing = new ObjectId().toHexString();
        mvc.perform(put(BASE+"/update-"+kind+"/"+missing).contentType("application/json").content(payload(kind)))
                .andExpect(status().isNotFound());
        mvc.perform(delete(BASE+"/delete-"+kind+"/"+missing)).andExpect(status().isNotFound());
    }
    @Test void mongooseCastingAndInvalidDates() throws Exception {
        mvc.perform(post(BASE+"/create-time").contentType("application/json").content("{\"time\":123}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.newTime.time").value("123"));
        mvc.perform(post(BASE+"/create-location").contentType("application/json").content("{\"location\":{}}"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Server error"));
        mvc.perform(post(BASE+"/create-trip").contentType("application/json")
                .content(payload("trip").replace("2026-09-21T08:00:00.000Z","not-a-date")))
                .andExpect(status().isInternalServerError());
    }
    @Test void initializesFortySeatsInOriginalCollectionAndResetsThem() throws Exception {
        initialize();
        JsonNode seats = body(mvc.perform(get(SEATS)).andExpect(status().isOk()).andReturn());
        assertThat(seats.size()).isEqualTo(40);
        assertThat(seats.get(0).get("seatNo").asText()).isEqualTo("01");
        assertThat(seats.get(39).get("seatNo").asText()).isEqualTo("40");
        for (JsonNode seat : seats) {
            assertThat(seat.get("bookingStatus").asText()).isEqualTo("unbooked");
            for (String field : List.of("studentId","studentMail","bookingDate","bookingTime")) assertThat(seat.get(field).isNull()).isTrue();
        }
        assertThat(mongo.getCollection("trip12345").countDocuments()).isEqualTo(40);
        assertThat(mongo.collectionExists("trip12345s")).isFalse();
        mvc.perform(put(SEATS+"/01").contentType("application/json").content(booking())).andExpect(status().isOk());
        initialize();
        mvc.perform(get(SEATS+"/booked")).andExpect(content().json("[]"));
        assertThat(mongo.getCollection("trip12345").countDocuments()).isEqualTo(40);
    }
    String booking() { return "{\"bookingStatus\":\"booked\",\"studentId\":\"12345\",\"studentMail\":\"student@example.test\"}"; }
    @Test void bookingReadStudentLookupAndCancellation() throws Exception {
        initialize();
        mvc.perform(put(SEATS+"/01").contentType("application/json").content(booking()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Seat updated successfully"))
                .andExpect(jsonPath("$.seat.bookingStatus").value("booked"))
                .andExpect(jsonPath("$.seat.bookingDate").isString()).andExpect(jsonPath("$.seat.bookingTime").isString());
        mvc.perform(get(SEATS+"/01")).andExpect(jsonPath("$.studentId").value("12345"));
        mvc.perform(get(SEATS+"/booked")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(SEATS+"/student/12345")).andExpect(status().isOk()).andExpect(jsonPath("$[0].seatNo").value("01"));
        mvc.perform(put(SEATS+"/01").contentType("application/json").content("{\"bookingStatus\":\"unbooked\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.seat.bookingStatus").value("unbooked"));
        JsonNode seat = body(mvc.perform(get(SEATS+"/01")).andReturn());
        for (String field : List.of("studentId","studentMail","bookingDate","bookingTime")) assertThat(seat.get(field).isNull()).isTrue();
        mvc.perform(get(SEATS+"/student/12345")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No bookings found for this student in this trip"));
    }
    @Test void seatValidationAndMissingSeats() throws Exception {
        initialize();
        mvc.perform(put(SEATS+"/01").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Booking status is required"));
        mvc.perform(put(SEATS+"/01").contentType("application/json").content("{\"bookingStatus\":\"booked\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Student details are required for booking"));
        mvc.perform(get(SEATS+"/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Seat not found"));
        mvc.perform(put(SEATS+"/99").contentType("application/json").content(booking())).andExpect(status().isNotFound());
        mvc.perform(get(BASE+"/seats/UNKNOWN")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void bulkUpdatePreservesOrderingNullsAndSharedTimestamp() throws Exception {
        initialize();
        String b=booking();
        String updates="{\"seats\":["+b.replace("{","{\"seatNo\":\"02\",")+","+
                b.replace("{","{\"seatNo\":\"01\",")+",{\"seatNo\":\"99\",\"bookingStatus\":\"unbooked\"}]}";
        JsonNode response=body(mvc.perform(put(SEATS).contentType("application/json").content(updates))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Seats updated successfully"))
                .andExpect(jsonPath("$.tripId").value(TRIP)).andReturn());
        JsonNode seats=response.get("seats");
        assertThat(seats.get(0).get("seatNo").asText()).isEqualTo("02");
        assertThat(seats.get(1).get("seatNo").asText()).isEqualTo("01");
        assertThat(seats.get(0).get("bookingDate")).isEqualTo(seats.get(1).get("bookingDate"));
        assertThat(seats.get(2).isNull()).isTrue();
        mvc.perform(get(SEATS+"/booked")).andExpect(jsonPath("$[0].seatNo").value("01"));
    }
    @ParameterizedTest @ValueSource(strings={"{}","{\"seats\":[]}","{\"seats\":{}}"})
    void invalidBulk(String payload) throws Exception {
        mvc.perform(put(SEATS).contentType("application/json").content(payload))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid seats data"));
    }
    @Test void bulkStudentValidationIs500AndOtherWritesStillComplete() throws Exception {
        initialize();
        String payload="{\"seats\":[{\"seatNo\":\"01\",\"bookingStatus\":\"booked\"},"+
                booking().replace("{","{\"seatNo\":\"02\",")+"]}";
        mvc.perform(put(SEATS).contentType("application/json").content(payload))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.error").value("Student details required for seat 01"));
        mvc.perform(get(SEATS+"/02")).andExpect(jsonPath("$.bookingStatus").value("booked"));
    }
    @Test void updateDoesNotAddEnumValidationThatNodeDidNotRun() throws Exception {
        initialize();
        mvc.perform(put(SEATS+"/01").contentType("application/json").content("{\"bookingStatus\":\"custom\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.seat.bookingStatus").value("custom"));
    }
    @Test void readsExistingMongooseBsonWithoutConvertingDatabase() throws Exception {
        ObjectId id=new ObjectId();
        mongo.getCollection("addtrips").insertOne(new Document("_id",id).append("busID","B1").append("tripID",TRIP)
                .append("startlocation","Campus").append("destination","City").append("departuretime","08:00")
                .append("date",Date.from(java.time.Instant.parse("2026-09-21T00:00:00Z"))).append("__v",0));
        mvc.perform(get(BASE+"/get-trip/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$._id").value(id.toHexString()))
                .andExpect(jsonPath("$.date").value("2026-09-21T00:00:00.000Z"));
        mongo.getCollection("trip12345").insertOne(new Document("_id",new ObjectId()).append("seatNo","01")
                .append("bookingStatus","booked").append("studentId","12345").append("__v",0));
        mvc.perform(get(SEATS+"/01")).andExpect(status().isOk()).andExpect(jsonPath("$.bookingStatus").value("booked"));
    }
    @Test void defaultCorsAndUnauthenticatedAccess() throws Exception {
        mvc.perform(options(SEATS).header("Origin","http://localhost:5173")
                .header("Access-Control-Request-Method","PUT").header("Access-Control-Request-Headers","content-type,authorization"))
                .andExpect(status().isNoContent()).andExpect(header().string("Access-Control-Allow-Origin","*"))
                .andExpect(header().string("Access-Control-Allow-Methods","GET,HEAD,PUT,PATCH,POST,DELETE"))
                .andExpect(header().string("Access-Control-Allow-Headers","content-type,authorization"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
        mvc.perform(get(BASE+"/get-trip").header("Origin","http://localhost:5174"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","*"));
        mvc.perform(head(BASE+"/get-trip")).andExpect(status().isOk());
        // Actual HEAD body suppression is a servlet-container responsibility; covered by the HTTP parity test.
        mvc.perform(get("/API/ADMIN/GET-TIME")).andExpect(status().isOk());
        mvc.perform(get(BASE+"/get-time/")).andExpect(status().isOk());
    }
    @Test void inactiveRoutesAndShadowedBusRouteStayInactive() throws Exception {
        mvc.perform(get("/api/user/getallusers")).andExpect(status().isNotFound());
        mvc.perform(post("/confirm-booking").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete(SEATS)).andExpect(status().isNotFound());
        mvc.perform(get(BASE+"/get-trip/BUS01")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Server error"));
    }
}
