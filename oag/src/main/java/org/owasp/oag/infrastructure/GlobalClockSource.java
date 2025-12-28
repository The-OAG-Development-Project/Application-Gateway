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

    /**
     * Default constructor for GlobalClockSource.
     * Initializes the global clock with the system UTC time.
     */
    public GlobalClockSource() {

        this.clock = Clock.systemUTC();
        log.info("Initialized global clock, its now   {}", clock.instant().toString());
    }

    /**
     * Gets the current global clock instance.
     * 
     * @return The current Clock instance
     */
    public Clock getGlobalClock() {
        return clock;
    }

    /**
     * Sets the global clock to a specific Clock instance.
     * 
     * @param clock The Clock instance to set as the global clock (must not be null)
     * @throws IllegalArgumentException if the provided clock is null
     */
    public void setGlobalClock(Clock clock) {

        if (clock == null)
            throw new IllegalArgumentException("Clock must not be null");

        this.clock = clock;
    }

    /**
     * Gets the current time as epoch seconds.
     * 
     * @return The current time in seconds since the epoch (January 1, 1970, 00:00:00 GMT)
     */
    public int getEpochSeconds() {

        return Math.toIntExact(clock.instant().getEpochSecond());
    }

    /**
     * This should be used for testing time dependent functionality only.
     * Advances the global clock by the specified number of seconds.
     *
     * @param seconds The number of seconds to advance the clock by
     */
    public void putClockForwardSeconds(int seconds) {

        setGlobalClock(Clock.offset(clock, Duration.ofSeconds(seconds)));
        log.warn("Put clock {} ahead of time. Its now {}. This is something used for testing only", seconds, clock.instant().toString());
    }
}
