package dev.rdcl.www.api.jwt;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;

@ConfigMapping(prefix = "app.jwt")
public interface JwtProperties {

    String issuer();

    @WithDefault("PT4H")
    Duration authTokenExpiry();

    @WithDefault("${mp.jwt.verify.publickey.location}")
    String publicKeyLocation();
}
