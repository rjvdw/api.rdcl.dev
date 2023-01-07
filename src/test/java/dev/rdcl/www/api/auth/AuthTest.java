package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.auth.fixtures.Identities;
import dev.rdcl.www.api.jwt.JwtService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;
import io.smallrye.jwt.auth.principal.JWTParser;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class AuthTest {

    @Inject
    JwtService jwtService;

    @Inject
    JWTParser jwtParser;

    @InjectMock
    AuthMailService authMailService;

    @Test
    @DisplayName("When an existing user tries to log in, they get a session token, and a verification code is mailed")
    public void testLogin() {
        login(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200);

        Mockito.verify(authMailService)
            .sendVerificationMail(
                ArgumentMatchers.eq(Identities.VALID_IDENTITY.getEmail()),
                ArgumentMatchers.any()
            );
    }

    @Test
    @DisplayName("When a non-existing user tries to log in, they get a session token, but no verification code is mailed")
    public void testLoginWithInvalidUser() {
        login(Identities.INVALID_IDENTITY.getEmail())
            .then().statusCode(200);

        Mockito.verify(authMailService, Mockito.never())
            .sendVerificationMail(
                ArgumentMatchers.eq(Identities.INVALID_IDENTITY.getEmail()),
                ArgumentMatchers.any()
            );
    }

    @Test
    @DisplayName("When a user verifies their log-in attempt they get a JWT")
    public void testVerifyLogin() throws Exception {
        ArgumentCaptor<String> verificationCodeCaptor = ArgumentCaptor.forClass(String.class);

        LoginResponse loginResponse = login(Identities.VALID_IDENTITY.getEmail())
            .then().statusCode(200)
            .extract().body().as(LoginResponse.class);

        Mockito.verify(authMailService)
            .sendVerificationMail(
                ArgumentMatchers.eq(Identities.VALID_IDENTITY.getEmail()),
                verificationCodeCaptor.capture()
            );

        VerificationResponse verificationResponse = verify(loginResponse, verificationCodeCaptor.getValue())
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

    private Response verify(LoginResponse loginResponse, String verificationCode) {
        String sessionToken = loginResponse.sessionToken();

        return verify(sessionToken, verificationCode);
    }

    private Response verify(String sessionToken, String verificationCode) {
        return given()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body("session-token=%s&verification-code=%s".formatted(sessionToken, verificationCode))
            .when().post("/auth/login/verify");
    }

}
