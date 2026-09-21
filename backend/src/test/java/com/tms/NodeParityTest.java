package com.tms;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.*;

/** Optional differential oracle. Run with -Dnode.reference=/absolute/path/to/original/backend (dependencies installed). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named="node.reference", matches=".+")
class NodeParityTest {
    static final MongoServer MONGO = new MongoServer(new MemoryBackend());
    static final InetSocketAddress ADDRESS = MONGO.bind();
    @DynamicPropertySource static void config(DynamicPropertyRegistry p) {
        p.add("spring.data.mongodb.uri", () -> "mongodb://localhost:" + ADDRESS.getPort() + "/java_parity");
        p.add("tms.booking-zone", () -> "Asia/Dhaka");
        p.add("tms.booking-locale", () -> "en-US");
    }
    @LocalServerPort int port;
    static Process node;
    static int nodePort;
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    static final ObjectMapper JSON = new ObjectMapper();
    static int comparisons;
    @BeforeAll static void startNode() throws Exception {
        try (var socket = new java.net.ServerSocket(0)) { nodePort=socket.getLocalPort(); }
        ProcessBuilder builder=new ProcessBuilder("node", "index.js");
        builder.directory(Path.of(System.getProperty("node.reference")).toFile());
        builder.environment().put("PORT", String.valueOf(nodePort));
        builder.environment().put("MONGO_URL", "mongodb://localhost:"+ADDRESS.getPort()+"/node_parity");
        builder.environment().put("TZ", "Asia/Dhaka");
        builder.environment().put("LANG", "en_US.UTF-8");
        builder.redirectErrorStream(true).redirectOutput(Path.of("target/node-parity.log").toFile());
        node=builder.start();
        for (int i=0; i<100; i++) {
            try { request(nodePort,"GET","/api/admin/get-time",null); return; }
            catch (Exception e) { if (!node.isAlive()) throw new IllegalStateException("Node exited; see target/node-parity.log",e); Thread.sleep(100); }
        }
        throw new IllegalStateException("Node did not start; see target/node-parity.log");
    }
    @AfterAll static void close() throws Exception {
        if(node!=null) { node.destroy(); if(!node.waitFor(5,java.util.concurrent.TimeUnit.SECONDS)) node.destroyForcibly(); }
        MONGO.shutdownNow();
        System.out.println("NODE_PARITY_COMPARISONS="+comparisons);
    }
    static HttpResponse<String> request(int port, String method, String path, String body) throws Exception {
        var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).timeout(Duration.ofSeconds(10))
                .header("Origin","http://localhost:5173");
        if(body!=null) req.header("Content-Type","application/json");
        if(method.equals("OPTIONS")) req.header("Access-Control-Request-Method","PUT").header("Access-Control-Request-Headers","content-type,authorization");
        return HTTP.send(req.method(method, body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    JsonNode[] compare(String method,String path,String body) throws Exception { return compare(method,path,path,body); }
    JsonNode[] compare(String method,String javaPath,String nodePath,String body) throws Exception {
        var actual=request(port,method,javaPath,body);
        var expected=request(nodePort,method,nodePath,body);
        String label=method+" "+javaPath+" body="+body;
        assertThat(actual.statusCode()).as(label).isEqualTo(expected.statusCode());
        for(String header:List.of("access-control-allow-origin","access-control-allow-methods","access-control-allow-headers","access-control-allow-credentials"))
            assertThat(actual.headers().firstValue(header)).as(label+" "+header).isEqualTo(expected.headers().firstValue(header));
        comparisons++;
        if(expected.headers().firstValue("content-type").orElse("").startsWith("application/json")&&!method.equals("HEAD")) {
            JsonNode a=JSON.readTree(actual.body()),e=JSON.readTree(expected.body());
            assertThat(normalize(a)).as(label).isEqualTo(normalize(e));
            return new JsonNode[]{a,e};
        }
        assertThat(actual.body()).as(label).isEqualTo(expected.body());
        return new JsonNode[]{NullNode.instance,NullNode.instance};
    }
    JsonNode normalize(JsonNode value) {
        if(value.isObject()) {
            ObjectNode copy=((ObjectNode)value).deepCopy();
            copy.fields().forEachRemaining(entry -> {
                String k=entry.getKey(); JsonNode v=entry.getValue();
                if(k.equals("_id")) { assertThat(v.asText()).matches("[0-9a-f]{24}"); copy.put(k,"OBJECT_ID"); }
                else if(k.equals("bookingDate")&&!v.isNull()) { assertThat(v.asText()).matches(".*T.*\\.\\d{3}Z"); copy.put(k,"SERVER_DATE"); }
                else if(k.equals("bookingTime")&&!v.isNull()) { assertThat(v.asText()).matches("\\d{1,2}:\\d{2}:\\d{2} [AP]M"); copy.put(k,"SERVER_TIME"); }
                else copy.set(k,normalize(v));
            });
            return copy;
        }
        if(value.isArray()) { ArrayNode copy=JSON.createArrayNode(); value.forEach(v -> copy.add(normalize(v))); return copy; }
        return value;
    }
    @Test void compareMountedApiWithOriginalNode() throws Exception {
        for(String kind:List.of("time","location","trip")) {
            String key=kind.substring(0,1).toUpperCase()+kind.substring(1);
            String payload=kind.equals("time") ? "{\"time\":\"08:00\"}" : kind.equals("location") ? "{\"location\":\"Campus\"}" :
                "{\"busID\":\"BUS01\",\"tripID\":\"TRIP12345\",\"startlocation\":\"Campus\",\"destination\":\"City\",\"date\":\"2026-09-21T00:00:00.000Z\",\"departuretime\":\"08:00\"}";
            String base="/api/admin/";
            compare("GET",base+"get-"+kind,null);
            compare("POST",base+"create-"+kind,"{}");
            compare("POST",base+"create-"+kind,null);
            JsonNode[] created=compare("POST",base+"create-"+kind,payload);
            String a=created[0].get("new"+key).get("_id").asText(),n=created[1].get("new"+key).get("_id").asText();
            compare("GET",base+"get-"+kind,null);
            compare("GET",base+"get-"+kind+"/"+a,base+"get-"+kind+"/"+n,null);
            compare("PUT",base+"update-"+kind+"/"+a,base+"update-"+kind+"/"+n,payload);
            compare("PUT",base+"update-"+kind+"/"+a,base+"update-"+kind+"/"+n,"{}");
            compare("GET",base+"get-"+kind+"/bad-id",null);
            compare("DELETE",base+"delete-"+kind+"/"+a,base+"delete-"+kind+"/"+n,null);
            compare("GET",base+"get-"+kind+"/"+a,base+"get-"+kind+"/"+n,null);
            compare("PUT",base+"update-"+kind+"/"+a,base+"update-"+kind+"/"+n,payload);
            compare("DELETE",base+"delete-"+kind+"/"+a,base+"delete-"+kind+"/"+n,null);
        }
        String s="/api/admin/seats/TRIP12345";
        compare("GET",s,null);
        compare("POST","/api/admin/seats/create/TRIP12345",null);
        compare("GET",s,null);
        compare("GET",s+"/01",null);
        compare("GET",s+"/99",null);
        compare("GET",s+"/booked",null);
        compare("GET",s+"/student/12345",null);
        compare("PUT",s+"/01","{}");
        compare("PUT",s+"/01","{\"bookingStatus\":\"booked\"}");
        String b="{\"bookingStatus\":\"booked\",\"studentId\":\"12345\",\"studentMail\":\"student@example.test\"}";
        compare("PUT",s+"/01",b);
        compare("GET",s+"/booked",null);
        compare("GET",s+"/student/12345",null);
        compare("PUT",s+"/99",b);
        compare("PUT",s,"{\"seats\":["+b.replace("{","{\"seatNo\":\"02\",")+",{\"seatNo\":\"99\",\"bookingStatus\":\"unbooked\"}]}");
        compare("PUT",s,"{\"seats\":[{\"seatNo\":\"01\",\"bookingStatus\":\"booked\"}]}");
        compare("PUT",s,"{\"seats\":[]}");
        compare("PUT",s,"{\"seats\":{}}");
        compare("PUT",s+"/01","{\"bookingStatus\":\"custom\"}");
        compare("PUT",s+"/01","{\"bookingStatus\":\"unbooked\"}");
        compare("GET",s+"/01",null);
        compare("POST","/api/admin/seats/create/TRIP12345",null);
        compare("GET",s+"/booked",null);
        compare("OPTIONS",s,null);
        compare("HEAD",s,null);
        compare("GET","/api/admin/get-time/",null);
        compare("GET","/API/ADMIN/GET-TIME",null);
        compare("GET","/api/user/getallusers",null);
        compare("POST","/confirm-booking","{}");
        compare("DELETE",s,null);
        compare("PATCH","/api/admin/get-time","{}");
        compare("GET","/api/admin/get-trip/BUS01",null);
    }
}
