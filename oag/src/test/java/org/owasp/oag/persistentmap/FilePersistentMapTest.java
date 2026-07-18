package org.owasp.oag.persistentmap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link FilePersistentMap} — every case touches the real filesystem, so
 * per the project directive these are integration tests. Each test gets an isolated
 * {@link TempDir}.
 */
class FilePersistentMapTest {

    @TempDir
    Path tempDir;

    private Path dbFile() {
        return tempDir.resolve("map.db");
    }

    private FilePersistentMap<Integer> open() {
        return new FilePersistentMap<>(dbFile().toString(), Integer.class);
    }

    private long lineCount() throws IOException {
        if (!Files.exists(dbFile())) {
            return 0;
        }
        return Files.readAllLines(dbFile(), StandardCharsets.UTF_8).stream()
                .filter(l -> !l.isEmpty()).count();
    }

    @Test
    void putThenGetAndRemove() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            assertEquals(1, map.get("a"));
            map.remove("a");
            assertNull(map.get("a"));
        }
    }

    @Test
    void reopeningRestoresLiveEntries() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            map.put("b", 2);
        }
        try (var reopened = open()) {
            assertEquals(1, reopened.get("a"));
            assertEquals(2, reopened.get("b"));
            assertEquals(2, reopened.size());
        }
    }

    @Test
    void removedKeyStaysAbsentAfterReopen() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            map.put("b", 2);
            map.remove("a");
        }
        try (var reopened = open()) {
            assertNull(reopened.get("a"));
            assertEquals(2, reopened.get("b"));
        }
    }

    @Test
    void appendAfterCompactionPersists() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            map.compact();      // force the writer close -> move -> reopen path
            map.put("b", 2);
        }
        assertEquals(2, lineCount());
        try (var reopened = open()) {
            assertEquals(1, reopened.get("a"));
            assertEquals(2, reopened.get("b"));
        }
    }

    @Test
    void autoCompactionBoundsFileSize() throws IOException {
        int writes = 3000;
        try (var map = open()) {
            for (int i = 0; i < writes; i++) {
                map.put("k" + (i % 3), i);   // only 3 live keys, many appends
            }
            assertEquals(3, map.size());
        }
        // The append-only log must not have grown to one line per write.
        assertTrue(lineCount() < writes / 2, "log should be compacted, was " + lineCount() + " lines");
        try (var reopened = open()) {
            assertEquals(3, reopened.size());
        }
    }

    @Test
    void reopenDoesNotReappend() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            map.put("b", 2);
        }
        long afterFirst = lineCount();
        try (var reopened = open()) {
            // just open and close, no writes
            assertEquals(2, reopened.size());
        }
        assertEquals(afterFirst, lineCount(), "reopening must not re-append existing entries");
    }

    @Test
    void malformedTrailingLineIsSkipped() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
        }
        // Simulate a torn write: append a partial/garbage line.
        Files.writeString(dbFile(), "{\"k\":\"b\",\"v\":",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        try (var reopened = open()) {
            assertEquals(1, reopened.get("a"));
            assertNull(reopened.get("b"));
        }
    }

    @Test
    void foreignBinaryFileIsMigratedCleanly() throws IOException {
        // Pre-existing non-JSON binary content (e.g. an old MapDB file).
        byte[] garbage = new byte[]{0x00, 0x01, 0x02, 'n', 'o', 't', ' ', 'j', 's', 'o', 'n', '\n', (byte) 0xC3, 0x28};
        Files.write(dbFile(), garbage);

        try (var map = open()) {
            assertEquals(0, map.size());     // unreadable content ignored
            map.put("x", 42);
        }
        // File was rewritten in the clean format; only the new entry remains.
        assertEquals(1, lineCount());
        try (var reopened = open()) {
            assertEquals(42, reopened.get("x"));
        }
    }

    @Test
    void keysWithSpecialCharactersRoundTrip() throws IOException {
        String weird = "line\nbreak\t\"quote\" and \\backslash";
        try (var map = open()) {
            map.put(weird, 7);
        }
        try (var reopened = open()) {
            assertEquals(7, reopened.get(weird));
            assertEquals(1, reopened.size());
        }
    }

    @Test
    void concurrentPutsPersist() throws Exception {
        int threads = 8;
        int perThread = 200;
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(threads);

        try (var map = open()) {
            for (int t = 0; t < threads; t++) {
                final int base = t * perThread;
                Thread thread = new Thread(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < perThread; i++) {
                            map.put("k" + (base + i), base + i);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
                thread.start();
            }
            start.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS));
            assertEquals(threads * perThread, map.size());
        }

        try (var reopened = open()) {
            assertEquals(threads * perThread, reopened.size());
            assertEquals(0, reopened.get("k0"));
        }
    }

    @Test
    void closeIsIdempotent() throws IOException {
        var map = open();
        map.put("a", 1);
        map.close();
        map.close();     // second close must not throw
    }

    @Test
    void putRejectsNullValue() throws IOException {
        try (var map = open()) {
            assertThrows(NullPointerException.class, () -> map.put("a", null));
        }
    }

    @Test
    void clearEmptiesTheFile() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            map.put("b", 2);
            map.clear();
            assertEquals(0, map.size());
        }
        assertEquals(0, lineCount());
        try (var reopened = open()) {
            assertEquals(0, reopened.size());
        }
    }

    @Test
    void entrySetReturnsSnapshot() throws IOException {
        try (var map = open()) {
            map.put("a", 1);
            var snapshot = map.entrySet();
            map.put("b", 2);
            assertEquals(1, snapshot.size());
            assertEquals(List.of("a"), snapshot.stream().map(java.util.Map.Entry::getKey).toList());
        }
    }
}
