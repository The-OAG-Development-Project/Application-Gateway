package org.owasp.oag.services.blacklist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.persistentmap.FilePersistentMap;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalPersistentBlacklistTest {

    @TempDir
    Path tempDir;

    private LocalPersistentBlacklist openBlacklist(GlobalClockSource clockSource) {
        String file = tempDir.resolve("session-blacklist.db").toString();
        return new LocalPersistentBlacklist(clockSource, new FilePersistentMap<>(file, Integer.class));
    }

    @Test
    public void testSessionBlacklist() throws IOException {

        var clockSource = new GlobalClockSource();
        var blacklist = openBlacklist(clockSource);
        // Store some values

        blacklist.invalidateSession("1", 100).block();
        blacklist.invalidateSession("2", 200).block();

        assertTrue(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());
        assertFalse(blacklist.isInvalidated("3").block());

        blacklist.close();

        // Reopen
        blacklist = openBlacklist(clockSource);
        assertTrue(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());
        assertFalse(blacklist.isInvalidated("3").block());


        blacklist.invalidateSession("3", 100).block();
        assertTrue(blacklist.isInvalidated("3").block());

        blacklist.close();
    }

    @Test
    public void testSessionBlacklistExpiry() throws IOException {

        var clockSource = new GlobalClockSource();
        var blacklist = openBlacklist(clockSource);

        // Store some values
        blacklist.invalidateSession("1", 100).block();
        blacklist.invalidateSession("2", 200).block();

        timeTravelToFuture(clockSource, 120);

        blacklist.cleanup().block();

        assertFalse(blacklist.isInvalidated("1").block());
        assertTrue(blacklist.isInvalidated("2").block());

        blacklist.close();
    }

    public static void timeTravelToFuture(GlobalClockSource clockSource, int seconds) {
        clockSource.setGlobalClock(Clock.offset(clockSource.getGlobalClock(), Duration.ofSeconds(seconds)));
    }
}
