package dev.rdcl.www.api.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.time.Clock;

@ApplicationScoped
public class ClockConfig {

    @Produces
    @ApplicationScoped
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
