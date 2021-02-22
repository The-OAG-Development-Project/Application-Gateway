package org.owasp.oag.services.blacklist;

import org.owasp.oag.GlobalClockSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalPersistentBlacklistTest {

    final static String testDbName = "session-blacklist-test.db";

    @Test
    public void testSessionBlacklist() throws IOException {

        var clockSource = new GlobalClockSource();
        var blacklist = new LocalPersistentBlacklist(clockSource, testDbName);
        // Store some values

        blacklist.invalidateSession("1", 100).block();
        blacklist.invalidateSession("2", 200).block();

        assertTrue(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());
        assertFalse(blacklist.isInvalidated("3").block());

        blacklist.close();

        // Reopen
        blacklist = new LocalPersistentBlacklist(clockSource, testDbName);
        assertTrue(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());
        assertFalse(blacklist.isInvalidated("3").block());


        blacklist.invalidateSession("3", 100).block();
        assertTrue(blacklist.isInvalidated("3").block());

        // Cleanup
        blacklist.close();
        new File(testDbName).delete();
    }

    @Test
    public void testSessionBlacklistExpiry() throws IOException {

        var clockSource = new GlobalClockSource();
        var blacklist = new LocalPersistentBlacklist(clockSource, testDbName);

        // Store some values
        blacklist.invalidateSession("1", 100).block();
        blacklist.invalidateSession("2", 200).block();

        timeTravelToFuture(clockSource, 120);

        blacklist.cleanup().block();

        assertFalse(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());

        // Cleanup
        blacklist.close();
        new File(testDbName).delete();
    }

    public static void timeTravelToFuture(GlobalClockSource clockSource, int seconds) {
        clockSource.setGlobalClock(Clock.offset(clockSource.getGlobalClock(), Duration.ofSeconds(seconds)));
    }
}