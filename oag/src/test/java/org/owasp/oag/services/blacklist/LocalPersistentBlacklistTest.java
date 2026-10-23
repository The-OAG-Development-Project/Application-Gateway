package org.owasp.oag.services.blacklist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.persistentmap.FilePersistentMap;
import org.owasp.oag.persistentmap.InMemoryPersistentMap;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the blocking contract of the local persistent session blacklist.
 */
class LocalPersistentBlacklistTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    @TempDir
    Path tempDir;

    private GlobalClockSource clockSourceAt(Instant now) {

        var clockSource = new GlobalClockSource();
        clockSource.setGlobalClock(Clock.fixed(now, ZoneId.of("UTC")));
        return clockSource;
    }

    private LocalPersistentBlacklist inMemoryBlacklist(GlobalClockSource clockSource) {
        return new LocalPersistentBlacklist(clockSource, new InMemoryPersistentMap<>());
    }

    private LocalPersistentBlacklist fileBackedBlacklist(GlobalClockSource clockSource) {
        String file = tempDir.resolve("session-blacklist.db").toString();
        return new LocalPersistentBlacklist(clockSource, new FilePersistentMap<>(file, Integer.class));
    }

    @Test
    void invalidatedSessionsAreReportedAsInvalidated() {

        var blacklist = inMemoryBlacklist(clockSourceAt(START));

        blacklist.invalidateSession("1", 100);
        blacklist.invalidateSession("2", 200);

        assertTrue(blacklist.isInvalidated("1"));
        assertTrue(blacklist.isInvalidated("2"));
        assertFalse(blacklist.isInvalidated("3"));
    }

    @Test
    void invalidatedSessionsSurviveReopeningTheBackingFile() throws IOException {

        var clockSource = clockSourceAt(START);

        var blacklist = fileBackedBlacklist(clockSource);
        blacklist.invalidateSession("1", 100);
        blacklist.invalidateSession("2", 200);
        blacklist.close();

        // Reopen: the bloom filter is rebuilt from the persisted entries
        blacklist = fileBackedBlacklist(clockSource);
        try {
            assertTrue(blacklist.isInvalidated("1"));
            assertTrue(blacklist.isInvalidated("2"));
            assertFalse(blacklist.isInvalidated("3"));

            // The reopened instance is still writable
            blacklist.invalidateSession("3", 100);
            assertTrue(blacklist.isInvalidated("3"));
        } finally {
            blacklist.close();
        }
    }

    @Test
    void cleanupRemovesEntriesWhoseTtlHasExpiredAndKeepsTheOthers() {

        var clockSource = clockSourceAt(START);
        var blacklist = inMemoryBlacklist(clockSource);

        blacklist.invalidateSession("1", 100);
        blacklist.invalidateSession("2", 200);

        // Move past the ttl of "1" but not of "2"
        clockSource.putClockForwardSeconds(150);
        blacklist.cleanup();

        assertFalse(blacklist.isInvalidated("1"));
        assertTrue(blacklist.isInvalidated("2"));
    }

    @Test
    void deleteAllEntriesClearsTheBlacklist() {

        var blacklist = inMemoryBlacklist(clockSourceAt(START));
        blacklist.invalidateSession("1", 100);

        blacklist.deleteAllEntries();

        assertFalse(blacklist.isInvalidated("1"));
    }
}
