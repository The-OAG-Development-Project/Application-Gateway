package org.owasp.oag;

import org.springframework.stereotype.Component;

import java.time.Clock;

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
