package dev.rdcl.www.api;

import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
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

    private final static String VALID_USER = "john.doe@example.com";
    private final static String INVALID_USER = "jane.doe@example.com";

    @Inject
    MockMailbox mailbox;

    @Inject
    JWTParser jwtParser;

    @BeforeEach
    public void setup() {
        mailbox.clear();
    }

    @Test
    @DisplayName("When an existing user tries to log in, they get a session token, and a verification code is mailed")
    public void testLogin() {
        login(VALID_USER)
            .then().statusCode(200);

        List<Mail> mails = mailbox.getMessagesSentTo(VALID_USER);
        assertThat(mails, hasSize(1));
    }

    @Test
    @DisplayName("When a non-existing user tries to log in, they get a session token, but no verification code is mailed")
    public void testLoginWithInvalidUser() {
        login(INVALID_USER)
            .then().statusCode(200);

        assertThat(mailbox.getTotalMessagesSent(), is(0));
    }

    @Test
    @DisplayName("When a user verifies their log-in attempt they get a JWT")
    public void testVerifyLogin() throws Exception {
        LoginResponse loginResponse = login(VALID_USER)
            .then().statusCode(200)
            .extract().body().as(LoginResponse.class);

        Mail mail = mailbox.getMessagesSentTo(VALID_USER).get(0);

        VerificationResponse verificationResponse = verify(loginResponse, mail)
            .then().statusCode(200)
            .extract().body().as(VerificationResponse.class);

        JsonWebToken jwt = jwtParser.parse(verificationResponse.jwt());
        assertThat(jwt.getSubject(), is("f277b076-f061-403c-bf7b-266eab926677"));
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
