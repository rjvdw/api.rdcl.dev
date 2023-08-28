package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.dto.InitiateLoginResult;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.auth.events.InitiateLoginAttemptCompleteEvent;
import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static dev.rdcl.www.api.auth.TestUtils.extractVerificationCode;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
public class AuthTest {

    @Inject
    JwtService jwtService;

    @Inject
    JWTParser jwtParser;

    @Inject
    MockMailbox mailbox;

    CountDownLatch pendingVerifications = new CountDownLatch(0);

    @BeforeEach
    public void setup() {
        mailbox.clear();
        pendingVerifications = new CountDownLatch(0);
    }

    public void consume(@ObservesAsync InitiateLoginAttemptCompleteEvent event) {
        pendingVerifications.countDown();
    }

    @Test
    @DisplayName("The public key used to verify an authentication token can be retrieved")
    public void testKey() {
        given()
            .when()
            .get("/auth/key")
            .then()
            .statusCode(200)
            .body(startsWith("-----BEGIN PUBLIC KEY-----"))
            .body(endsWith("-----END PUBLIC KEY-----\n"));
    }

    @Test
    @DisplayName("When an existing user tries to log in, they get a session token, and a verification code is mailed")
    public void testLogin() {
        pendingVerifications = new CountDownLatch(1);

        callLogin(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200);

        verifyMails(1);
    }

    @Test
    @DisplayName("A valid callback can be provided")
    public void testLoginWithValidCallback() {
        pendingVerifications = new CountDownLatch(1);

        callLogin(Identities.VALID_IDENTITY.getEmail(), "https://example.com/login/verify")
            .then().statusCode(200);

        List<Mail> mails = verifyMails(1);

        Mail mail = mails.get(0);
        assertThat(mail.getHtml(), containsString("https://example.com/login/verify"));
    }

    @Test
    @DisplayName("An error is returned when a malformed callback is provided")
    public void testLoginWithMalformedCallback() {
        callLogin(Identities.VALID_IDENTITY.getEmail(), "malformed-uri")
            .then().statusCode(400);
    }

    @Test
    @DisplayName("An error is returned when a callback which is not allowed is provided")
    public void testLoginWithIllegalCallback() {
        callLogin(Identities.VALID_IDENTITY.getEmail(), "https://example.com/not-allowed/login")
            .then().statusCode(400);
    }

    @Test
    @DisplayName("When a non-existing user tries to log in, they get a session token, but no verification code is mailed")
    public void testLoginWithInvalidUser() {
        pendingVerifications = new CountDownLatch(1);

        callLogin(Identities.INVALID_IDENTITY.getEmail())
            .then().statusCode(200);

        verifyMails(0);
    }

    @Test
    @DisplayName("When a user verifies their log-in attempt they get a JWT")
    public void testVerifyLogin() throws Exception {
        pendingVerifications = new CountDownLatch(1);

        InitiateLoginResult loginResponse = callLogin(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200)
            .extract().body().as(InitiateLoginResult.class);

        List<Mail> mails = verifyMails(1);

        String verificationCode = extractVerificationCode(mails.get(0));

        VerificationResponse verificationResponse = callVerify(loginResponse, verificationCode)
            .then().statusCode(200)
            .extract().body().as(VerificationResponse.class);

        JsonWebToken jwt = jwtParser.parse(verificationResponse.jwt());
        assertThat(jwt.getSubject(), is("f277b076-f061-403c-bf7b-266eab926677"));
    }

    @Test
    @DisplayName("When a user tries to verify their log-in with an invalid or expired session they get an error response")
    public void testVerifyLoginInvalid() {
        callVerify("invalid session token", "invalid verification code")
            .then().statusCode(401);
    }

    @Test
    @DisplayName("Authenticated users can view their profile")
    public void testMeAuthenticated() {
        String jwt = jwtService.issueAuthToken(Identities.VALID_IDENTITY);
        callMe(jwt).then().statusCode(200);
    }

    @Test
    @DisplayName("Authenticated users can update their profile")
    public void testPatchMeAuthenticated() {
        String jwt = jwtService.issueAuthToken(Identities.VALID_IDENTITY);

        callMe(jwt)
            .then()
            .statusCode(200)
            .body("name", is(Identities.VALID_IDENTITY.getName()));

        callPatchMe(jwt, "New Name")
            .then()
            .statusCode(200)
            .body("name", is("New Name"));

        callMe(jwt)
            .then()
            .statusCode(200)
            .body("name", is("New Name"));

        callPatchMe(jwt, Identities.VALID_IDENTITY.getName())
            .then()
            .statusCode(200)
            .body("name", is(Identities.VALID_IDENTITY.getName()));
    }

    @Test
    @DisplayName("Unauthenticated users cannot view their profile")
    public void testMeUnauthenticated() {
        callMe().then().statusCode(401);
    }

    private Response callMe() {
        return given().when().get("/auth/me");
    }

    private Response callMe(String jwt) {
        return given()
            .header("Authorization", "Bearer %s".formatted(jwt))
            .when().get("/auth/me");
    }

    private Response callPatchMe(String jwt, String name) {
        return given()
            .header("Authorization", "Bearer %s".formatted(jwt))
            .formParam("name", name)
            .when().patch("/auth/me");
    }

    private Response callLogin(String email) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("email", email)
            .when().post("/auth/login");
    }

    private Response callLogin(String email, String callback) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("email", email)
            .formParam("callback", callback)
            .when().post("/auth/login");
    }

    private Response callVerify(InitiateLoginResult loginResponse, String verificationCode) {
        String sessionToken = loginResponse.payload();

        return callVerify(sessionToken, verificationCode);
    }

    private Response callVerify(String sessionToken, String verificationCode) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("session-token", sessionToken)
            .formParam("verification-code", verificationCode)
            .when().post("/auth/login/verify");
    }

    private List<Mail> verifyMails(int expected) {
        try {
            pendingVerifications.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Mail> mails = mailbox.getMailsSentTo(Identities.VALID_IDENTITY.getEmail());
        assertThat(mails, hasSize(expected));

        return mails;
    }

}
