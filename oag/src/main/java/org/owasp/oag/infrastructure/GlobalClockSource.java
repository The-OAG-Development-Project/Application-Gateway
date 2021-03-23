package org.owasp.oag.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

/**
 * This bean is a global source of a Clock object. It is for testing to change the system wide time.
 * All parts of OAG should use this bean to access the current time.
 */
@Component
public class GlobalClockSource {

    private static final Logger log = LoggerFactory.getLogger(GlobalClockSource.class);
    private Clock clock;

    public GlobalClockSource() {

        this.clock = Clock.systemUTC();
        log.info("Initialized global clock, its now   {}", clock.instant().toString());
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

    /**
     * This should be used for testing time dependent functionality only
     * @param seconds
     */
    public void putClockForwardSeconds(int seconds){

        setGlobalClock(Clock.offset(clock, Duration.ofSeconds(seconds)));
        log.warn("Put clock {} ahead of time. Its now {}. This is something used for testing only", seconds, clock.instant().toString());
    }
}
