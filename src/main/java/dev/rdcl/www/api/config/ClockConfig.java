package dev.rdcl.www.api.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

@ApplicationScoped
public class ClockConfig {

    @Produces
    @ApplicationScoped
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
