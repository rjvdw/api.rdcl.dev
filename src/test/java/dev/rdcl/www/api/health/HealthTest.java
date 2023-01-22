package dev.rdcl.www.api.health;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.util.ClockTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
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

        for (LocalDate date = LocalDate.parse("2012-01-01"), upto = LocalDate.parse("2012-01-15");
             date.isBefore(upto);
             date = date.plusDays(1)
        ) {
            given()
                .spec(withJsonBody("{}"))
                .when()
                .put(url(date))
                .then()
                .statusCode(204);
        }

        given()
            .when()
            .get(url())
            .then()
            .statusCode(200)
            .body("health", hasSize(10));

        given()
            .when()
            .get(url(null, "2012-01-05"))
            .then()
            .statusCode(200)
            .body("health", hasSize(5));

        given()
            .when()
            .get(url("2012-01-01", null))
            .then()
            .statusCode(200)
            .body("health", hasSize(10));

        given()
            .when()
            .get(url("2012-01-01", "2012-01-05"))
            .then()
            .statusCode(200)
            .body("health", hasSize(5));

        given()
            .when()
            .get(url("2012-01-10", null))
            .then()
            .statusCode(200)
            .body("health", hasSize(5));

        for (LocalDate date = LocalDate.parse("2012-01-05"), upto = LocalDate.parse("2012-01-15");
             date.isBefore(upto);
             date = date.plusDays(1)
        ) {
            given()
                .when()
                .delete(url(date))
                .then()
                .statusCode(204);
        }

        given()
            .when()
            .get(url("2012-01-01", null))
            .then()
            .statusCode(200)
            .body("health", hasSize(4));

        given()
            .spec(withJsonBody("[]"))
            .when()
            .put(url("2012-01-01"))
            .then()
            .statusCode(204);

        given()
            .when()
            .get(url("2012-01-01", "2012-01-01"))
            .then()
            .statusCode(200)
            .body("health", hasSize(1))
            .body("health[0].data", is("[]"));
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("Reject invalid JSON")
    public void testRejectInvalidJson() {

        given()
            .spec(withJsonBody("}{"))
            .when()
            .put(url("2012-01-01"))
            .then()
            .statusCode(400);
    }

    private String url() {
        return "/health";
    }

    private String url(Object date) {
        return "/health/%s".formatted(date);
    }

    private String url(Object from, Object to) {
        if (from != null && to != null) return "/health?from=%s&to=%s".formatted(from, to);
        if (from != null) return "/health?from=%s".formatted(from);
        if (to != null) return "/health?to=%s".formatted(to);
        return url();
    }

    private RequestSpecification withJsonBody(String data) {
        return given()
            .header("Content-Type", "application/json")
            .body(data);
    }
}
