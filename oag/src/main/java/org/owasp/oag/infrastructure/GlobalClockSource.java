package org.owasp.oag.infrastructure;

import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * This bean is a global source of a Clock object. It is for testing to change the system wide time.
 * All parts of OAG should use this bean to access the current time.
 */
@Component
public class GlobalClockSource {

    private Clock clock;

    public GlobalClockSource() {
        this.clock = Clock.systemUTC();
    }

    public Clock getGlobalClock() {
        return clock;
    }

    public void setGlobalClock(Clock clock) {

        if (clock == null)
            throw new IllegalArgumentException("Clock must not be null");

        this.clock = clock;
    }

    public int getEpochSeconds() {

        return Math.toIntExact(clock.instant().getEpochSecond());
    }
}
