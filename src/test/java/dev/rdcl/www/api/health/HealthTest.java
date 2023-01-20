package dev.rdcl.www.api.health;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.health.dto.ListHealthResponse;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.util.ClockTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class HealthTest {

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
    @DisplayName("Create health records, update health records, delete health records")
    public void testEndpoints() {
        ClockTestUtils.setTime(clock, "2012-01-15T12:00:00Z");

        ListHealthResponse response;

        for (LocalDate date = LocalDate.parse("2012-01-01"), upto = LocalDate.parse("2012-01-15");
             date.isBefore(upto);
             date = date.plusDays(1)
        ) {
            save(date, "{}").then().statusCode(204);
        }

        response = listRecent().then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(10));

        response = listRecent("2012-01-05").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(5));

        response = list("2012-01-01").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(10));

        response = list("2012-01-01", "2012-01-05").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(5));

        response = list("2012-01-10").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(5));

        for (LocalDate date = LocalDate.parse("2012-01-05"), upto = LocalDate.parse("2012-01-15");
             date.isBefore(upto);
             date = date.plusDays(1)
        ) {
            delete(date).then().statusCode(204);
        }

        response = list("2012-01-01").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health(), hasSize(4));

        save("2012-01-01", "[]").then().statusCode(204);

        response = list("2012-01-01", "2012-01-01").then()
            .statusCode(200)
            .extract().as(ListHealthResponse.class);
        assertThat(response.health().get(0).data(), is("[]"));
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("Reject invalid JSON")
    public void testRejectInvalidJson() {
        save("2020-01-01", "}{").then().statusCode(400);
    }

    private Response listRecent() {
        return given()
            .when().get("/health");
    }

    private Response listRecent(Object upto) {
        return given()
            .when().get("/health?to=%s".formatted(upto));
    }

    private Response list(Object from) {
        return given()
            .when().get("/health?from=%s".formatted(from));
    }

    private Response list(Object from, Object to) {
        return given()
            .when().get("/health?from=%s&to=%s".formatted(from, to));
    }

    private Response save(Object date, String data) {
        return given()
            .header("Content-Type", "application/json")
            .body(data)
            .when().put("/health/%s".formatted(date));
    }

    private Response delete(Object date) {
        return given()
            .when().delete("/health/%s".formatted(date));
    }
}
