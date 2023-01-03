package dev.rdcl.www.api.jwt;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.jwt")
public interface JwtProperties {
    String issuer();
}
