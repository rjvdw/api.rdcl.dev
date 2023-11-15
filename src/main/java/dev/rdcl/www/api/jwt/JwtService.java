package dev.rdcl.www.api.jwt;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.util.Resources;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Returns the key that is used to verify tokens.
     *
     * @return The public key.
     * @throws IOException When the file could not be read.
     */
    public byte[] getPublicKey() throws IOException {
        return Resources.readResource(jwtProperties.publicKeyLocation());
    }

    /**
     * Issue an authorization token.
     *
     * @param identity The identity for which the authorization token is issued.
     * @return An authorization token.
     */
    public String issueAuthToken(Identity identity) {
        return Jwt
                .issuer(jwtProperties.issuer())
                .subject(identity.getId().toString())
                .upn(identity.getEmail())
                .groups(Set.of("user"))
                .preferredUserName(identity.getName())
                .issuedAt(Instant.now())
                .expiresIn(jwtProperties.authTokenExpiry())
                .sign();
    }

    public Optional<UUID> verifyAuthTokenOptional(JsonWebToken jwt, SecurityContext ctx) {
        return Optional.ofNullable(ctx.getUserPrincipal())
                .filter(principal -> principal.getName().equals(jwt.getName()))
                .map(principal -> jwt)
                .map(JsonWebToken::getSubject)
                .map(UUID::fromString);
    }

    public UUID verifyAuthToken(JsonWebToken jwt, SecurityContext ctx) {
        return verifyAuthTokenOptional(jwt, ctx)
                .orElseThrow(() -> new WebApplicationException(Response.Status.UNAUTHORIZED));
    }

}
