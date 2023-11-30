package com.example.rmm.devices.controller;

import com.example.rmm.devices.controller.dtos.ModifyDeviceServicesRequest;
import com.example.rmm.devices.controller.dtos.ModifyDeviceServicesRequest.Action;
import com.example.rmm.devices.controller.dtos.SaveDeviceRequest;
import com.example.rmm.devices.controller.dtos.Type;
import com.example.rmm.devices.service.LocalCache;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DeviceControllerTest {

    private static final String NAME_REQUIRED = "The System Name is required";
    private static final String TYPE_REQUIRED = "The Type is required";
    private static final String CUSTOMER_ID_REQUIRED = "The Customer ID is required";
    private static final String CUSTOMER_ID_INVALID = "The Customer ID is invalid";
    private static final float BASE_SERVICE_COST = 4f;
    public static final int OK = 200;
    public static final int CONFLICT = 409;
    public static final int NOT_FOUND = 404;
    public static final int BAD_REQUEST = 400;
    public static final String BASE_SERVICE_NAME = "Device of any type";
    public static final int BASE_SERVICE_ID = 1;
    public static final int WINDOWS_ANTIVIRUS = 2;
    public static final int MAC_ANTIVIRUS = 3;
    public static final int BACKUP = 4;
    public static final int SCREEN_SHARE = 5;

    final SaveDeviceRequest TEST_DEVICE = new SaveDeviceRequest("Server1", Type.WINDOWS_SERVER, 1L);

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LocalCache cache;

    @BeforeAll
    public void beforeAll() {
        RestAssured.port = port;
    }

    @AfterEach
    public void afterEach() {
        deleteAllFromTable("device");
        cache.clear();
    }

    @Nested
    class SaveDeviceWithValidRequest {

        @Test
        void shouldSaveSuccessfully() {
            given().
                    contentType(JSON).
                    body(TEST_DEVICE).
            when().
                    post("/devices").
            then().
                    statusCode(OK)
                    .body("id", equalTo(1));
        }

        @Test
        void shouldNotAllowDuplicates() {
            // Save the device
            given().contentType(JSON).body(TEST_DEVICE).post("/devices").then().statusCode(OK);

            // Verify it was NOT saved - Duplicated error
            given().
                    contentType(JSON).
                    body(TEST_DEVICE).
            when().
                    post("/devices").
            then().
                    statusCode(CONFLICT)
                    .body("errors.size()", equalTo(1))
                    .body("errors[0]", equalTo("Provided data is violating a data integrity constraint"));
        }
    }

    @Test
    void shouldRetrieveFullDeviceDataById() {
        // Save the device
        given().contentType(JSON).body(TEST_DEVICE).post("/devices");

        // Validate it's retrieved with its expected properties
        when().
                get("/devices/{id}", 1).
        then().
                statusCode(OK)
                .body(
                        "id", equalTo(1),
                        "systemName", equalTo(TEST_DEVICE.systemName()),
                        "customerId", equalTo(TEST_DEVICE.customerId().intValue()),
                        "servicesCost", equalTo(BASE_SERVICE_COST),
                        "services.size()", equalTo(1)
                )
                // Check that the base service is attached to the device
                .rootPath("services[0]")
                    .body(
                            "id", equalTo(BASE_SERVICE_ID),
                            "name", equalTo(BASE_SERVICE_NAME),
                            "price", equalTo(BASE_SERVICE_COST)
                    );
    }

    @Test
    void shouldRetrieveAllDevices() {
        // Validate return empty array
        when().get("/devices").then().statusCode(OK).body("$.size()", equalTo(0));

        // Save two devices
        final var device1 = new SaveDeviceRequest("David's Mac", Type.MAC, 1L);
        final var device2 = new SaveDeviceRequest("David's Ubuntu", Type.LINUX, 1L);
        given().contentType(JSON).body(device1).post("/devices");
        given().contentType(JSON).body(device2).post("/devices");

        // Validate that an array with size 2 is returned
        when().
                get("/devices").
        then().
                statusCode(OK)
                .body("$.size()", equalTo(2));
    }

    @Test
    void shouldDeleteTheDevice() {
        // Save the device
        given().contentType(JSON).body(TEST_DEVICE).post("/devices");

        // Verify it was persisted
        when().get("/devices/{id}", 1).then().statusCode(OK).body("id", equalTo(1));

        // Delete the device
        when().delete("/devices/{id}", 1).then().statusCode(OK);

        // Verify it was removed - Not found
        when().
                get("/devices/{id}", 1).
        then().
                statusCode(NOT_FOUND)
                .body("errors.size()", equalTo(1))
                .body("errors[0]", equalTo("Device not found"));
    }

    @Nested
    class SaveDeviceWithInvalidRequest {

        @Test
        void shouldFailWhenRequiredPropertiesAreNotProvided() {
            final List<String> errors =
                given().
                        contentType(JSON).
                        body(SaveDeviceRequest.builder().build()).
                when().
                        post("/devices").
                then().
                        statusCode(BAD_REQUEST)
                        .extract()
                        .path("errors");

            assertThat(errors).containsExactlyInAnyOrder(
                    NAME_REQUIRED,
                    TYPE_REQUIRED,
                    CUSTOMER_ID_REQUIRED
            );
        }

        @Test
        void shouldFailForInvalidCustomerId() {
            final var device = SaveDeviceRequest.builder().customerId(0L).build();

            final List<String> errors =
                    given().
                            contentType(JSON).
                            body(device).
                    when().
                            post("/devices").
                    then().
                            statusCode(BAD_REQUEST).
                    extract().
                            path("errors");

            assertThat(errors).containsExactlyInAnyOrder(
                    NAME_REQUIRED,
                    TYPE_REQUIRED,
                    CUSTOMER_ID_INVALID
            );
        }
    }

    @Nested
    class ModifyDeviceServices {

        @Test
        void shouldAddServicesToDevice() {
            // Save the device
            given().contentType(JSON).body(TEST_DEVICE).post("/devices");

            // Create the request to add services
            final var request = buildModifyDeviceServicesRequest(Action.ADD, WINDOWS_ANTIVIRUS, SCREEN_SHARE);

            // Send the request to add services
            given().
                    contentType(JSON).
                    body(request).
            when().
                    post("/devices/{id}/services", 1).
            then().
                    statusCode(OK);

            // Validate it returns the persisted services + the base service
            when().
                    get("/devices/{id}", 1).
            then().
                    statusCode(OK)
                    .body(
                            "services.size()", equalTo(3),
                            "services.id", containsInAnyOrder(
                                    BASE_SERVICE_ID,
                                    WINDOWS_ANTIVIRUS,
                                    SCREEN_SHARE
                            )
                    );
        }

        @Test
        void shouldRemoveServicesToDevice() {
            // Save the device
            given().contentType(JSON).body(TEST_DEVICE).post("/devices");

            // Create the request to ADD services
            final var addServicesRequest =
                    buildModifyDeviceServicesRequest(Action.ADD, WINDOWS_ANTIVIRUS, SCREEN_SHARE, BACKUP);

            // Send the request to add some services
            given().contentType(JSON).body(addServicesRequest).post("/devices/{id}/services", 1);

            // Create the request to REMOVE services
            final var removeServicesRequest =
                    buildModifyDeviceServicesRequest(Action.REMOVE, WINDOWS_ANTIVIRUS, SCREEN_SHARE);

            // Send the request to remove some services
            given().contentType(JSON).body(removeServicesRequest).post("/devices/{id}/services", 1);

            // Validate it does NOT return the removed services
            when().
                    get("/devices/{id}", 1).
            then().
                    statusCode(OK)
                    .body(
                            "services.size()", equalTo(2),
                            "services.id", containsInAnyOrder(BASE_SERVICE_ID, BACKUP)
                    );
        }

        @Test
        void shouldCalculateTotalCostForCustomer() {
            /*
                Customer with 2 Windows and 3 Macs, with the following Services:

                | Device Type | Antivirus | Backup | Screen Share |
                |-------------|-----------|--------|--------------|
                | Windows     | 2         | 1      | 2            |
                | Mac         | 3         | 2      | 2            |
                | Total       | 31        | 9      | 4            |

                Total Cost: $64
                Explanation:
                  Devices cost: $20
                  Antivirus cost: $31
                  Backup: $9
                  Screen Share: $4
             */

            final var customerId = 1L;

            // Save devices
            final var windows1 = new SaveDeviceRequest("Windows1", Type.WINDOWS_SERVER, customerId);
            final var windows2 = new SaveDeviceRequest("Windows2", Type.WINDOWS_SERVER, customerId);
            final var mac1 = new SaveDeviceRequest("Mac1", Type.MAC, customerId);
            final var mac2 = new SaveDeviceRequest("Mac2", Type.MAC, customerId);
            final var mac3 = new SaveDeviceRequest("Mac3", Type.MAC, customerId);
            given().contentType(JSON).body(windows1).post("/devices");
            given().contentType(JSON).body(windows2).post("/devices");
            given().contentType(JSON).body(mac1).post("/devices");
            given().contentType(JSON).body(mac2).post("/devices");
            given().contentType(JSON).body(mac3).post("/devices");

            // Add services to Windows1
            given().contentType(JSON).body(
                    buildModifyDeviceServicesRequest(Action.ADD, WINDOWS_ANTIVIRUS, SCREEN_SHARE, BACKUP)
            ).post("/devices/{id}/services", 1);

            // Add services to Windows2
            given().contentType(JSON).body(
                    buildModifyDeviceServicesRequest(Action.ADD, WINDOWS_ANTIVIRUS, SCREEN_SHARE)
            ).post("/devices/{id}/services", 2);

            // Add services to Mac1
            given().contentType(JSON).body(
                    buildModifyDeviceServicesRequest(Action.ADD, MAC_ANTIVIRUS, SCREEN_SHARE, BACKUP)
            ).post("/devices/{id}/services", 3);

            // Add services to Mac2
            given().contentType(JSON).body(
                    buildModifyDeviceServicesRequest(Action.ADD, MAC_ANTIVIRUS, SCREEN_SHARE, BACKUP)
            ).post("/devices/{id}/services", 4);

            // Add services to Mac3
            given().contentType(JSON).body(
                    buildModifyDeviceServicesRequest(Action.ADD, MAC_ANTIVIRUS)
            ).post("/devices/{id}/services", 5);

            // Sum the cost for all the devices cached
            // From device ID 1 (inclusive) to 6 (exclusive) - IDs: 1, 2, 3, 4, 5
            final var devicesServicesCost =
                    IntStream.range(1, 6)
                             .boxed()
                             .mapToDouble(id -> cache.get((long) id, () -> 0.0))
                             .sum();

            // The cost of all the services for the 5 devices should be $64
            assertThat(devicesServicesCost).isEqualTo(64);
        }
    }

    private static ModifyDeviceServicesRequest buildModifyDeviceServicesRequest(
            final Action action,
            final int... services
    ) {

        requireNonNull(action);
        requireNonNull(services);

        return new ModifyDeviceServicesRequest(
                IntStream.of(services).boxed().map(s -> (long) s).collect(toUnmodifiableSet()),
                action
        );
    }

    private void deleteAllFromTable(final String tableName) {
        jdbcTemplate.execute(format("DELETE FROM %s", tableName));
        jdbcTemplate.execute(format("ALTER TABLE %s ALTER COLUMN id RESTART WITH 1", tableName));
    }
}