package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.dto.ActivityRequest;
import dev.rdcl.www.api.activities.dto.ActivityResponse;
import dev.rdcl.www.api.activities.dto.ListActivitiesResponse;
import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.util.ClockTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.Clock;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class ActivityTest {

    @InjectMock
    JwtService jwtService;

    @Inject
    Clock clock;

    @BeforeEach
    public void setup() {
        Mockito.when(jwtService.verifyJwt(any(), any()))
            .thenReturn(Identities.VALID_IDENTITY.getId());
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("User creates, updates and deletes an activity")
    public void testEndpoints() {
        ClockTestUtils.setTime(clock, "2000-01-01T00:00:00Z");

        ActivityRequest createRequest = new ActivityRequest(
            "test activity",
            null,
            null,
            "http://example.com/test-activity",
            "test location",
            "2022-05-05T12:00:00+02:00",
            null,
            false
        );

        create(createRequest).then().statusCode(200);

        ListActivitiesResponse listResponse1 = list().then().statusCode(200)
            .extract().body().as(ListActivitiesResponse.class);

        assertThat(listResponse1.activities(), hasSize(1));
        assertThat(listResponse1.activities().get(0).url(), is("http://example.com/test-activity"));

        UUID id = listResponse1.activities().get(0).id();

        ActivityResponse getResponse = get(id).then().statusCode(200)
            .extract().body().as(ActivityResponse.class);

        assertThat(getResponse, is(listResponse1.activities().get(0)));

        ActivityRequest updateRequest = new ActivityRequest(
            "updated test activity",
            "with a description",
            "with some notes",
            "http://example.com/updated-test-activity",
            "updated test location",
            "2022-05-05T14:00:00+02:00",
            "2022-05-05T16:00:00+02:00",
            true
        );

        ActivityResponse updateResponse = update(id, updateRequest).then().statusCode(200)
            .extract().body().as(ActivityResponse.class);

        assertThat(updateResponse.url(), is("http://example.com/updated-test-activity"));

        remove(id).then().statusCode(204);

        ListActivitiesResponse listResponse2 = list().then().statusCode(200)
            .extract().body().as(ListActivitiesResponse.class);

        assertThat(listResponse2.activities(), hasSize(0));
    }

    private Response list() {
        return given().when().get(url());
    }

    private Response get(UUID id) {
        return given().when().get(url(id));
    }

    private Response create(ActivityRequest data) {
        return withData(data).when().post(url());
    }

    private Response update(UUID id, ActivityRequest data) {
        return withData(data).when().put(url(id));
    }

    private Response remove(UUID id) {
        return given().when().delete(url(id));
    }

    private RequestSpecification withData(ActivityRequest data) {
        return given()
            .formParam("title", data.getTitle())
            .formParam("description", data.getDescription())
            .formParam("notes", data.getNotes())
            .formParam("url", data.getUrl())
            .formParam("location", data.getLocation())
            .formParam("starts", data.getStarts())
            .formParam("ends", data.getEnds())
            .formParam("allDay", data.isAllDay());
    }

    private String url() {
        return "/activity";
    }

    private String url(UUID id) {
        return "/activity/%s".formatted(id);
    }

}
