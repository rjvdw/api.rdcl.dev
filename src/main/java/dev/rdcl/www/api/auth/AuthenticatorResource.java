package dev.rdcl.www.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.rdcl.www.api.auth.dto.AuthenticatorResponse;
import dev.rdcl.www.api.auth.dto.RegisterResult;
import dev.rdcl.www.api.auth.entities.Authenticator;
import dev.rdcl.www.api.auth.errors.CredentialJsonException;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/auth/authenticator")
@RequiredArgsConstructor
public class AuthenticatorResource {

    private final AuthenticatorService authenticatorService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    @POST
    @Path("/")
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
        @Context SecurityContext ctx
    ) {
        UUID owner = jwtService.verifyAuthToken(jwt, ctx);
        try {
            RegisterResult result = authenticatorService.register(owner);
            return Response
                .ok()
                .header("Access-Control-Expose-Headers", "Location")
                .header("Location", "/auth/authenticator/%s/complete-registration".formatted(result.id()))
                .entity(result.options())
                .build();
        } catch (JsonProcessingException e) {
            throw new CredentialJsonException(e);
        }
    }

    @POST
    @Path("/{id}/complete-registration")
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void completeRegistration(
        @Context SecurityContext ctx,
        @PathParam("id") UUID registrationId,
        @Valid @Json String credentialJson
    ) {
        UUID owner = jwtService.verifyAuthToken(jwt, ctx);
        try {
            authenticatorService.completeRegistration(owner, registrationId, credentialJson);
        } catch (JsonProcessingException e) {
            throw new CredentialJsonException(e);
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("user")
    @Consumes({
        MediaType.APPLICATION_FORM_URLENCODED,
        MediaType.MULTIPART_FORM_DATA,
    })
    public AuthenticatorResponse update(
        @Context SecurityContext ctx,
        @PathParam("id") UUID id,
        @Valid @FormParam("name") @Size(max = 255) String name
    ) {
        UUID owner = jwtService.verifyAuthToken(jwt, ctx);
        Authenticator authenticator = authenticatorService.update(owner, id, entity -> {
            entity.setName(name);
        });

        return new AuthenticatorResponse(authenticator.getId(), authenticator.getName());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("user")
    public void remove(
        @Context SecurityContext ctx,
        @PathParam("id") UUID id
    ) {
        UUID owner = jwtService.verifyAuthToken(jwt, ctx);
        authenticatorService.remove(owner, id);
    }
}
