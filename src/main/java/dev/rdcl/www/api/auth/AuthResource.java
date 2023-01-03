package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.dto.IdentityResponse;
import dev.rdcl.www.api.auth.dto.LoginResponse;
import dev.rdcl.www.api.auth.dto.VerificationResponse;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

@Path("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    @GET
    @Path("/me")
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public IdentityResponse me(@Context SecurityContext ctx) {
        UUID id = jwtService.verifyJwt(jwt, ctx);
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
        String email
    ) {
        String sessionToken = authService.initiateLogin(email);

        return new LoginResponse(sessionToken);
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
        String jwt = jwtService.issueJwt(identity);

        return new VerificationResponse(jwt);
    }
}

