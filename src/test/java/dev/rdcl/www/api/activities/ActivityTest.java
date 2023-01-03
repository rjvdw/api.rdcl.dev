package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.dto.ActivityRequest;
import dev.rdcl.www.api.activities.dto.ListActivitiesResponse;
import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class ActivityTest {

    @InjectMock
    JwtService jwtService;

    @BeforeEach
    public void setup() {
        Mockito.when(jwtService.verifyJwt(any(), any()))
            .thenReturn(Identities.VALID_IDENTITY.getId());
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("When a user creates an activity, they are able to retrieve that activity, and then delete it")
    public void testCreateAndListActivity() {
        ActivityRequest request = new ActivityRequest(
            "test activity",
            null,
            null,
            "http://example.com/test-activity",
            "test location",
            "2022-05-05T12:00:00+02:00",
            null,
            false
        );

        create(request).then().statusCode(200);

        ListActivitiesResponse response1 = list().then().statusCode(200)
            .extract().body().as(ListActivitiesResponse.class);

        assertThat(response1.activities(), hasSize(1));

        UUID id = response1.activities().get(0).id();

        remove(id).then().statusCode(204);

        ListActivitiesResponse response2 = list().then().statusCode(200)
            .extract().body().as(ListActivitiesResponse.class);

        assertThat(response2.activities(), hasSize(0));
    }

    private Response list() {
        return given()
            .when().get("/activity");
    }

    private Response create(ActivityRequest data) {
        return given()
            .formParam("title", data.getTitle())
            .formParam("description", data.getDescription())
            .formParam("notes", data.getNotes())
            .formParam("url", data.getUrl())
            .formParam("location", data.getLocation())
            .formParam("starts", data.getStarts())
            .formParam("ends", data.getEnds())
            .formParam("allDay", data.isAllDay())
            .when().post("/activity");
    }

    private Response remove(UUID id) {
        return given()
            .when().delete("/activity/%s".formatted(id));
    }

}
