package dev.rdcl.www.api.health;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.health")
public interface HealthProperties {

    @WithDefault("500")
    int maxResults();
}
