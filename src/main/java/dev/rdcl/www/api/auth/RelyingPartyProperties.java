package dev.rdcl.www.api.auth;

import io.smallrye.config.ConfigMapping;

import java.util.Set;

@ConfigMapping(prefix = "app.relying-party")
public interface RelyingPartyProperties {

    String id();

    String name();

    Set<String> origins();

    boolean allowOriginSubdomain();
}
