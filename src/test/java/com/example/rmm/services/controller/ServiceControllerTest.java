package com.example.rmm.services.controller;

import com.example.rmm.services.controller.dtos.SaveServiceRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ServiceControllerTest {

    public static final int OK = 200;
    public static final int CONFLICT = 409;
    public static final SaveServiceRequest TEST_SERVICE = new SaveServiceRequest("Add Blocker", 12.0f);

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    public void beforeAll() {
        RestAssured.port = port;
    }

    @BeforeEach
    public void afterEach() {
        deleteAllFromTable("service");
    }

    @Nested
    class SaveServiceWithValidRequest {

        @Test
        void shouldSaveSuccessfully() {
            given().
                    contentType(JSON).
                    body(TEST_SERVICE).
            when().
                    post("/services").
            then().
                    statusCode(OK)
                    .body("id", equalTo(1));
        }

        @Test
        void shouldNotAllowDuplicates() {
            // Save the service
            given().contentType(JSON).body(TEST_SERVICE).post("/services").then().statusCode(OK);

            // Verify it was NOT saved - Duplicated error
            given().
                    contentType(JSON).
                    body(TEST_SERVICE).
            when().
                    post("/services").
            then().
                    statusCode(CONFLICT)
                    .body("errors.size()", equalTo(1))
                    .body("errors[0]", equalTo("Provided data is violating a data integrity constraint"));
        }
    }

    @Test
    void shouldRetrieveAllServices() {
        // Validate return empty array
        when().get("/services").then().statusCode(OK).body("$.size()", equalTo(0));

        // Save two services
        final var service1 = new SaveServiceRequest("Add Blocker", 12.0f);
        final var service2 = new SaveServiceRequest("Screen Saver", 27.0f);
        given().contentType(JSON).body(service1).post("/services");
        given().contentType(JSON).body(service2).post("/services");

        // Validate that an array with size 2 is returned
        when().
                get("/services").
        then().
                statusCode(OK)
                .body(
                        "$.size()", equalTo(2),
                        "id", containsInAnyOrder(1, 2),
                        "name", containsInAnyOrder("Add Blocker", "Screen Saver"),
                        "price", containsInAnyOrder(12.0f, 27.0f)
                );
    }

    @Test
    void shouldDeleteTheService() {
        // Save the service
        given().contentType(JSON).body(TEST_SERVICE).post("/services");

        // Verify it was persisted
        when().get("/services").then().statusCode(OK).body("id", hasItem(1));

        // Delete the service
        when().delete("/services/{id}", 1).then().statusCode(OK);

        // Verify it was removed
        when().
                get("/services").
        then().
                statusCode(OK)
                .body("errors.size()", equalTo(0));
    }

    private void deleteAllFromTable(final String tableName) {
        jdbcTemplate.execute(format("DELETE FROM %s", tableName));
        jdbcTemplate.execute(format("ALTER TABLE %s ALTER COLUMN id RESTART WITH 1", tableName));
    }
}