package dev.rdcl.www.api.label;

import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.label.dto.ListLabelsResponse;
import dev.rdcl.www.api.label.entities.Label;
import dev.rdcl.www.api.restconfig.validators.Json;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/label")
@RequiredArgsConstructor
public class LabelResource {

    private final LabelService labelService;

    private final JwtService jwtService;

    private final JsonWebToken jwt;

    @GET
    @RolesAllowed("user")
    @Produces(MediaType.APPLICATION_JSON)
    public ListLabelsResponse list(@Context SecurityContext ctx) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        List<Label> labels = labelService.get(ownerId);

        return ListLabelsResponse.from(labels);
    }

    @POST
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(
        @Valid Map<@NotNull @Size(max = 31) String, @NotNull @Json String> labelConfigs,
        @Context SecurityContext ctx
    ) {
        UUID ownerId = jwtService.verifyAuthToken(jwt, ctx);
        List<Label> labels = labelConfigs.entrySet()
            .stream()
            .map(entry -> Label.builder()
                .text(entry.getKey())
                .styles(entry.getValue())
                .build())
            .toList();

        labelService.update(ownerId, labels);
    }
}
