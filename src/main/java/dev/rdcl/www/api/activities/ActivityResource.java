package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/activity")
@RequiredArgsConstructor
public class ActivityResource {

    private final ActivityService activityService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    private final Clock clock;

    @GET
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public ListResponse list(
        @Context SecurityContext ctx,
        @QueryParam("past") @DefaultValue("false") boolean getPastActivities,
        @QueryParam("when") Instant when
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        if (when == null) {
            when = Instant.now(clock);
        }

        List<Activity> activities = getPastActivities
            ? activityService.getPastActivities(ownerId, when)
            : activityService.getUpcomingActivities(ownerId, when);

        return new ListResponse(activities);
    }

    @GET
    @Path("/{uuid}")
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Activity> get(
        @PathParam("uuid") UUID activityId,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);

        return activityService.getActivity(ownerId, activityId);
    }

    @POST
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Activity create(
        @BeanParam @Valid Activity activity,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        activityService.createActivity(ownerId, activity);

        return activity;
    }

    @PUT
    @Path("/{uuid}")
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Activity update(
        @PathParam("uuid") UUID activityId,
        @BeanParam @Valid Activity activity,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        activityService.updateActivity(ownerId, activityId, activity);

        return activity;
    }

    @DELETE
    @Path("/{uuid}")
    @RolesAllowed("user")
    public void remove(
        @PathParam("uuid") UUID activityId,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        activityService.deleteActivity(ownerId, activityId);
    }

    public static record ListResponse(List<Activity> activities) {
    }
}
