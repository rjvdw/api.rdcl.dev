package dev.rdcl.www.api.label;

import dev.rdcl.www.api.jwt.JwtService;
import dev.rdcl.www.api.label.dto.ListLabelsResponse;
import dev.rdcl.www.api.label.entities.Label;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
