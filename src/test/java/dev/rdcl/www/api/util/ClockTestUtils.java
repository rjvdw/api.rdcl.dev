package dev.rdcl.www.api.util;

import io.quarkus.test.junit.QuarkusMock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class ClockTestUtils {

    public static void setTime(Clock clock, String instant) {
        setTime(clock, Instant.parse(instant));
    }

    public static void setTime(Clock clock, Instant instant) {
        QuarkusMock.installMockForInstance(Clock.fixed(instant, ZoneId.of("UTC")), clock);
    }

}
