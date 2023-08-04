package dev.rdcl.www.api.health;

import dev.rdcl.www.api.health.entity.Health;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/health")
@RequiredArgsConstructor
public class HealthResource {

    private final HealthService healthService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    private final Clock clock;

    @GET
    @Path("/settings")
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSettings(
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);

        return healthService.getSettings(ownerId);
    }

    @POST
    @Path("/settings")
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSettings(
        @Context SecurityContext ctx,
        @Valid @Json String settings
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        healthService.saveSettings(ownerId, settings);
    }

    @GET
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public ListHealthResponse list(
        @QueryParam("from") LocalDate from,
        @QueryParam("to") LocalDate to,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        List<Health> health;
        long count;
        if (from == null && to == null) {
            LocalDate now = LocalDate.now(clock);
            health = healthService.findBefore(ownerId, now);
            count = healthService.countBefore(ownerId, now);
        } else if (from == null) {
            health = healthService.findBefore(ownerId, to);
            count = healthService.countBefore(ownerId, to);
        } else if (to == null) {
            health = healthService.findAfter(ownerId, from);
            count = healthService.countAfter(ownerId, from);
        } else {
            health = healthService.findBetween(ownerId, from, to);
            count = healthService.countBetween(ownerId, from, to);
        }

        return new ListHealthResponse(health, count);
    }

    @PUT
    @Path("/{date}")
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void save(
        @PathParam("date") LocalDate date,
        @Context SecurityContext ctx,
        @Valid @Json String data
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        healthService.save(date, ownerId, data);
    }

    @DELETE
    @Path("/{date}")
    @RolesAllowed("user")
    public void delete(
        @PathParam("date") LocalDate date,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        healthService.delete(date, ownerId);
    }

    public record ListHealthResponse(List<Health> health, long count) {
    }
}
