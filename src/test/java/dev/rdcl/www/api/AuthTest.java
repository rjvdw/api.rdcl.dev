package dev.rdcl.www.api;

import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.smallrye.jwt.auth.principal.JWTParser;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class AuthTest {

    @Inject
    MockMailbox mailbox;

    @Inject
    JwtService jwtService;

    @Inject
    JWTParser jwtParser;

    @BeforeEach
    public void setup() {
        mailbox.clear();
    }

    @Test
    @DisplayName("When an existing user tries to log in, they get a session token, and a verification code is mailed")
    public void testLogin() {
        login(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200);

        List<Mail> mails = mailbox.getMessagesSentTo(Identities.VALID_IDENTITY.getEmail());
        assertThat(mails, hasSize(1));
    }

    @Test
    @DisplayName("When a non-existing user tries to log in, they get a session token, but no verification code is mailed")
    public void testLoginWithInvalidUser() {
        login(Identities.INVALID_IDENTITY.getEmail())
            .then().statusCode(200);

        assertThat(mailbox.getTotalMessagesSent(), is(0));
    }

    @Test
    @DisplayName("When a user verifies their log-in attempt they get a JWT")
    public void testVerifyLogin() throws Exception {
        LoginResponse loginResponse = login(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200)
            .extract().body().as(LoginResponse.class);

        Mail mail = mailbox.getMessagesSentTo(Identities.VALID_IDENTITY.getEmail()).get(0);

        VerificationResponse verificationResponse = verify(loginResponse, mail)
            .then().statusCode(200)
            .extract().body().as(VerificationResponse.class);

        JsonWebToken jwt = jwtParser.parse(verificationResponse.jwt());
        assertThat(jwt.getSubject(), is("f277b076-f061-403c-bf7b-266eab926677"));
    }

    @Test
    @DisplayName("When a user tries to verify their log-in with an invalid or expired session they get an error response")
    public void testVerifyLoginInvalid() {
        verify("invalid session token", "invalid verification code")
            .then().statusCode(401);
    }

    @Test
    @DisplayName("Authenticated users can view their profile")
    public void testMeAuthenticated() {
        String jwt = jwtService.issueJwt(Identities.VALID_IDENTITY);
        me(jwt).then().statusCode(200);
    }

    @Test
    @DisplayName("Unauthenticated users cannot view their profile")
    public void testMeUnauthenticated() {
        me().then().statusCode(401);
    }

    private Response me() {
        return given().when().get("/auth/me");
    }

    private Response me(String jwt) {
        return given()
            .header("Authorization", "Bearer %s".formatted(jwt))
            .when().get("/auth/me");
    }

    private Response login(String email) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body("email=%s".formatted(email))
            .when().post("/auth/login");
    }

    private Response verify(LoginResponse loginResponse, Mail verificationMail) {
        String sessionToken = loginResponse.sessionToken();
        String verificationCode = extractVerificationCode(verificationMail).get();

        return verify(sessionToken, verificationCode);
    }

    private Response verify(String sessionToken, String verificationCode) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body("session-token=%s&verification-code=%s".formatted(sessionToken, verificationCode))
            .when().post("/auth/login/verify");
    }

    private Optional<String> extractVerificationCode(Mail mail) {
        String html = mail.getHtml();

        int from = html.indexOf("<pre>");
        if (from == -1) {
            return Optional.empty();
        }
        from += "<pre>".length();

        int to = html.indexOf("</pre>", from);
        if (to == -1) {
            return Optional.empty();
        }

        return Optional.of(html.substring(from, to));
    }

}
