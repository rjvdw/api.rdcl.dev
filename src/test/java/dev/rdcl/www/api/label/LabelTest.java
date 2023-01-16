package dev.rdcl.www.api.label;

import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.label.dto.LabelConfig;
import dev.rdcl.www.api.label.dto.ListLabelsResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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
        Map<String, LabelConfig> labels = Map.of(
            "label1", new LabelConfig(null, null),
            "label2", new LabelConfig("#000", "#fff"),
            "label3", new LabelConfig("red", null),
            "label4", new LabelConfig("#f8f8f8", "#000")
        );

        update(labels).then().statusCode(204);

        ListLabelsResponse response = get()
            .then().statusCode(200)
            .extract().as(ListLabelsResponse.class);

        assertThat(response.labels().keySet(), hasSize(4));
        assertThat(response.labels(), hasEntry("label1", new LabelConfig(null, null)));
        assertThat(response.labels(), hasEntry("label2", new LabelConfig("#000", "#fff")));
        assertThat(response.labels(), hasEntry("label3", new LabelConfig("red", null)));
        assertThat(response.labels(), hasEntry("label4", new LabelConfig("#f8f8f8", "#000")));

        labels = Map.of(
            "label3", new LabelConfig("red", null),
            "label4", new LabelConfig("#000", "#fff"),
            "label5", new LabelConfig(null, null)
        );

        update(labels).then().statusCode(204);

        response = get()
            .then().statusCode(200)
            .extract().as(ListLabelsResponse.class);

        assertThat(response.labels().keySet(), hasSize(3));
        assertThat(response.labels(), hasEntry("label3", new LabelConfig("red", null)));
        assertThat(response.labels(), hasEntry("label4", new LabelConfig("#000", "#fff")));
        assertThat(response.labels(), hasEntry("label5", new LabelConfig(null, null)));
    }

    private Response get() {
        return given()
            .when().get("/label");
    }

    private Response update(Map<String, LabelConfig> labels) {
        return given()
            .body(labels)
            .header("Content-Type", "application/json")
            .when().post("/label");
    }
}
