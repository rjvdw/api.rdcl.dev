package dev.rdcl.www.api.jwt;

import dev.rdcl.www.api.auth.entities.Identity;
import io.smallrye.jwt.build.Jwt;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class JwtService {

    private static final Duration JWT_EXPIRES_IN = Duration.of(4, ChronoUnit.HOURS);

    private final JwtProperties jwtProperties;

    public String issueJwt(Identity identity) {
        return Jwt
            .issuer(jwtProperties.issuer())
            .subject(identity.getId().toString())
            .upn(identity.getEmail())
            .groups("user")
            .preferredUserName(identity.getName())
            .issuedAt(Instant.now())
            .expiresIn(JWT_EXPIRES_IN)
            .sign();
    }

    public Optional<UUID> verifyJwtOptional(JsonWebToken jwt, SecurityContext ctx) {
        return Optional.ofNullable(ctx.getUserPrincipal())
            .filter(principal -> principal.getName().equals(jwt.getName()))
            .map(principal -> jwt)
            .map(JsonWebToken::getSubject)
            .map(UUID::fromString);
    }

    public UUID verifyJwt(JsonWebToken jwt, SecurityContext ctx) {
        return verifyJwtOptional(jwt, ctx)
            .orElseThrow(() -> new WebApplicationException(Response.Status.UNAUTHORIZED));
    }

}
