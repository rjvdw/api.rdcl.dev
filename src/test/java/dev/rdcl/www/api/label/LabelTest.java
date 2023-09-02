package dev.rdcl.www.api.label;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.label.dto.ListLabelsResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class LabelTest {

    @InjectMock
    JwtService jwtService;

    @BeforeEach
    public void setup() {
        Mockito.when(jwtService.verifyAuthToken(any(), any()))
            .thenReturn(Identities.VALID_IDENTITY.getId());
    }

    @Test
    @TestSecurity(user = "john.doe@example.com", roles = {"user"})
    @DisplayName("User creates and then fetches labels")
    public void testEndpoints() {
        Map<String, String> labels = Map.of(
            "label1", "{}",
            "label2", "{\"prop\":\"val1\"}",
            "label3", "{\"prop\":\"val2\"}",
            "label4", "{\"prop\":\"val3\"}"
        );

        update(labels).then().statusCode(204);

        ListLabelsResponse response = get()
            .then().statusCode(200)
            .extract().as(ListLabelsResponse.class);

        assertThat(response.labels().keySet(), hasSize(4));
        assertThat(response.labels(), hasEntry("label1", "{}"));
        assertThat(response.labels(), hasEntry("label2", "{\"prop\": \"val1\"}"));
        assertThat(response.labels(), hasEntry("label3", "{\"prop\": \"val2\"}"));
        assertThat(response.labels(), hasEntry("label4", "{\"prop\": \"val3\"}"));

        labels = Map.of(
            "label3", "{\"prop\":\"val10\"}",
            "label4", "{\"prop\":\"val11\"}",
            "label5", "{}"
        );

        update(labels).then().statusCode(204);

        response = get()
            .then().statusCode(200)
            .extract().as(ListLabelsResponse.class);

        assertThat(response.labels().keySet(), hasSize(3));
        assertThat(response.labels(), hasEntry("label3", "{\"prop\": \"val10\"}"));
        assertThat(response.labels(), hasEntry("label4", "{\"prop\": \"val11\"}"));
        assertThat(response.labels(), hasEntry("label5", "{}"));
    }

    private Response get() {
        return given()
            .when().get("/label");
    }

    private Response update(Map<String, String> labels) {
        return given()
            .body(labels)
            .header("Content-Type", "application/json")
            .when().post("/label");
    }
}
