package dev.rdcl.www.api.settings;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class SettingsTest {

    @InjectMock
    JwtService jwtService;

    @BeforeEach
    public void setup() {
        Mockito.when(jwtService.verifyAuthToken(any(), any()))
                .thenReturn(Identities.VALID_IDENTITY.getId());
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("Fetch and update user settings")
    public void testSettingsEndpoints() {
        given()
                .when()
                .get("/settings")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("", aMapWithSize(0));

        given()
                .contentType("application/json")
                .body("{\"foo\": \"bar\"}")
                .when()
                .post("/settings")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/settings")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("", aMapWithSize(1))
                .body("foo", is("bar"));
    }
}
