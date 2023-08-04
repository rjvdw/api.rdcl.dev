package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.util.ClockTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class ActivityTest {

    @InjectMock
    JwtService jwtService;

    @Inject
    Clock clock;

    @BeforeEach
    public void setup() {
        Mockito.when(jwtService.verifyAuthToken(any(), any()))
            .thenReturn(Identities.VALID_IDENTITY.getId());
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("User creates, updates and deletes an activity")
    public void testEndpoints() {
        ClockTestUtils.setTime(clock, "2000-01-01T00:00:00Z");

        String insertedId = given()
            .formParam("title", "test activity")
            .formParam("url", "http://example.com/test-activity")
            .formParam("location", "test location")
            .formParam("timezone", "Europe/Amsterdam")
            .formParam("starts", "2022-05-05T12:00:00+06:00")
            .formParam("allDay", false)
            .when()
            .post(url())
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("owner", nullValue())
            .body("title", is("test activity"))
            .body("description", nullValue())
            .body("notes", nullValue())
            .body("url", is("http://example.com/test-activity"))
            .body("location", is("test location"))
            .body("timezone", is("Europe/Amsterdam"))
            .body("starts", is("2022-05-05T08:00+02:00"))
            .body("ends", nullValue())
            .body("allDay", is(false))
            .extract()
            .path("id");

        given()
            .when()
            .get(url())
            .then()
            .statusCode(200)
            .body("activities", hasSize(1))
            .body("activities[0].id", is(insertedId))
            .body("activities[0].owner", nullValue())
            .body("activities[0].url", is("http://example.com/test-activity"))
            .body("activities[0].starts", is("2022-05-05T08:00+02:00"));

        given()
            .when()
            .get(url(insertedId))
            .then()
            .statusCode(200)
            .body("id", is(insertedId))
            .body("owner", nullValue())
            .body("url", is("http://example.com/test-activity"))
            .body("starts", is("2022-05-05T08:00+02:00"));

        given()
            .formParam("title", "updated test activity")
            .formParam("description", "with a description")
            .formParam("notes", "with some notes")
            .formParam("url", "http://example.com/updated-test-activity")
            .formParam("location", "updated test location")
            .formParam("timezone", "America/Los_Angeles")
            .formParam("starts", "2022-05-05T14:00:00+06:00")
            .formParam("ends", "2022-12-05T16:00:00+06:00")
            .formParam("allDay", true)
            .when()
            .put(url(insertedId))
            .then()
            .statusCode(200)
            .body("id", is(insertedId))
            .body("owner", nullValue())
            .body("title", is("updated test activity"))
            .body("description", is("with a description"))
            .body("notes", is("with some notes"))
            .body("url", is("http://example.com/updated-test-activity"))
            .body("location", is("updated test location"))
            .body("timezone", is("America/Los_Angeles"))
            .body("starts", is("2022-05-05T01:00-07:00"))
            .body("ends", is("2022-12-05T02:00-08:00"))
            .body("allDay", is(true));

        given()
            .when()
            .delete(url(insertedId))
            .then()
            .statusCode(204);

        given()
            .when()
            .get(url())
            .then()
            .statusCode(200)
            .body("activities", hasSize(0));
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("User is not allowed to specify their own IDs")
    public void testDisallowSpecifyingId() {
        String id = UUID.randomUUID().toString();

        String insertedId = given()
            .formParam("id", id)
            .formParam("title", "test activity")
            .formParam("location", "test location")
            .formParam("timezone", "Europe/Amsterdam")
            .formParam("starts", "2022-05-05T12:00:00+06:00")
            .formParam("allDay", false)
            .when()
            .post(url())
            .then()
            .statusCode(200)
            .body("id", is(not(id)))
            .extract()
            .path("id");

        given()
            .formParam("id", id)
            .formParam("title", "test activity")
            .formParam("location", "test location")
            .formParam("timezone", "Europe/Amsterdam")
            .formParam("starts", "2022-05-05T12:00:00+06:00")
            .formParam("allDay", false)
            .when()
            .put(url(insertedId))
            .then()
            .statusCode(200)
            .body("id", is(insertedId));
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("User input is validated")
    public void testValidation() {
        // no parameters
        given()
            .when().post(url())
            .then()
            .statusCode(400);

        // invalid date
        given()
            .formParam("title", "test activity")
            .formParam("location", "test location")
            .formParam("timezone", "Europe/Amsterdam")
            .formParam("starts", "not a valid date")
            .when().post(url())
            .then()
            .statusCode(400);
    }

    private String url() {
        return "/activity";
    }

    private String url(Object id) {
        return "/activity/%s".formatted(id);
    }

}
