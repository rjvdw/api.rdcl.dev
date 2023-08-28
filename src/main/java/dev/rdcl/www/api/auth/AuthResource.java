package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.dto.IdentityResponse;
import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.errors.InvalidCallback;
import dev.rdcl.www.api.jwt.JwtService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.validator.constraints.URL;

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

    @PATCH
    @Path("/me")
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public IdentityResponse updateProfile(
        @Context SecurityContext ctx,

        @FormParam("name")
        @Valid
        @Size(max = 255)
        String name
    ) {
        UUID id = jwtService.verifyAuthToken(jwt, ctx);
        Identity identity = authService.updateUser(id, entity -> {
            if (name != null) entity.setName(name);
        });

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
