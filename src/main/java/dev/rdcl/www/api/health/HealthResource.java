package dev.rdcl.www.api.health;

import dev.rdcl.www.api.health.entity.Health;
import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.restconfig.validators.Json;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
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
