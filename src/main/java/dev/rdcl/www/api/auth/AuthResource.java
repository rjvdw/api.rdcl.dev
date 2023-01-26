package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.dto.IdentityResponse;
import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.errors.InvalidCallback;
import dev.rdcl.www.api.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.validator.constraints.URL;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Path("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    @GET
    @Path("/key")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public byte[] key() {
        try {
            return jwtService.getPublicKey();
        } catch (IOException ex) {
            throw new WebApplicationException("Unable to read public key", ex);
        }
    }

    @GET
    @Path("/me")
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public IdentityResponse me(@Context SecurityContext ctx) {
        UUID id = jwtService.verifyAuthToken(jwt, ctx);
        Identity identity = authService.getUser(id);

        return IdentityResponse.from(identity);
    }

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public LoginResponse login(
        @FormParam("email")
        @Valid
        @NotNull
        @Email
        String email,

        @FormParam("callback")
        @Valid
        @URL
        String callback
    ) {
        try {
            URI uri = callback == null ? null : new java.net.URL(callback).toURI();
            String sessionToken = authService.initiateLogin(email, uri);

            return new LoginResponse(sessionToken);
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new InvalidCallback(callback, ex);
        }
    }

    @POST
    @Path("/login/verify")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public VerificationResponse verify(
        @FormParam("session-token")
        @Valid
        @NotNull
        @Size(min = 10, max = 511)
        String sessionToken,

        @FormParam("verification-code")
        @Valid
        @NotNull
        @Size(min = 10, max = 511)
        String verificationCode
    ) {
        Identity identity = authService.verifyLogin(sessionToken, verificationCode);
        String authToken = jwtService.issueAuthToken(identity);

        return new VerificationResponse(authToken);
    }
}

