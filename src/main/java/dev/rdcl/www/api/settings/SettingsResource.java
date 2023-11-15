package dev.rdcl.www.api.settings;

import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/settings")
@RequiredArgsConstructor
public class SettingsResource {

    private final SettingsService settingsService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    @GET
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSettings(
            @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);

        return settingsService.getSettings(ownerId);
    }

    @POST
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSettings(
            @Context SecurityContext ctx,
            @Valid @Json String settings
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        settingsService.saveSettings(ownerId, settings);
    }
}
