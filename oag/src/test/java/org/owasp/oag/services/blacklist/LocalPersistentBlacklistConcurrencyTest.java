package org.owasp.oag.services.blacklist;

import org.junit.jupiter.api.Test;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.persistentmap.InMemoryPersistentMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that cleanup() can run concurrently with invalidateSession without losing an entry.
 * <p>
 * cleanup() replaces the bloom filter. If it does so without accounting for entries added while it
 * runs, isInvalidated takes its "not in bloom filter" short-circuit and reports a logged-out session
 * as still valid - so this is a security property, not a tidiness one.
 * <p>
 * This test is probabilistic but <em>one-sided</em>: it asserts an invariant that must always hold
 * (every identifier that was invalidated reads back as invalidated), so it can never fail
 * spuriously. It can only ever fail to <em>detect</em> a defect. A failure here is therefore always
 * a real bug and must never be dismissed as flakiness, retried away or disabled.
 */
class LocalPersistentBlacklistConcurrencyTest {

    /** Number of threads invalidating sessions while cleanup() runs. */
    private static final int WRITER_THREADS = 4;

    /**
     * Number of identifiers each writer thread invalidates. Large enough that the writers stay busy
     * across many cleanup() passes, which is what gives the race a chance to be observed.
     */
    private static final int IDS_PER_WRITER = 5000;

    /**
     * Invalidates identifiers from several threads while cleanup() runs continuously and asserts that
     * every identifier is still reported as invalidated afterwards.
     *
     * @throws Exception if a thread is interrupted while the test waits for the writers
     */
    @Test
    void cleanupRunningConcurrentlyNeverLosesAnInvalidatedSession() throws Exception {

        var clockSource = new GlobalClockSource();
        var blacklist = new LocalPersistentBlacklist(clockSource, new InMemoryPersistentMap<>());

        var startGate = new CountDownLatch(1);
        var writersDone = new CountDownLatch(WRITER_THREADS);
        var keepCleaning = new AtomicBoolean(true);
        var failures = new ArrayList<String>();
        var writtenIds = new ArrayList<String>();

        // Hammer cleanup() while the writers run
        var cleaner = new Thread(() -> {
            while (keepCleaning.get()) {
                blacklist.cleanup();
            }
        });

        var writers = new ArrayList<Thread>();
        for (int w = 0; w < WRITER_THREADS; w++) {
            final int writer = w;
            writers.add(new Thread(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < IDS_PER_WRITER; i++) {
                        var id = "writer-" + writer + "-id-" + i;
                        // A long ttl, so nothing expires during the test
                        blacklist.invalidateSession(id, 3600);
                        synchronized (writtenIds) {
                            writtenIds.add(id);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    writersDone.countDown();
                }
            }));
        }

        cleaner.start();
        writers.forEach(Thread::start);
        startGate.countDown();

        assertTrue(writersDone.await(30, TimeUnit.SECONDS), "writers did not finish");
        keepCleaning.set(false);
        cleaner.join();

        // Every id written must still be reported as invalidated
        List<String> ids;
        synchronized (writtenIds) {
            ids = List.copyOf(writtenIds);
        }
        for (var id : ids) {
            if (!blacklist.isInvalidated(id)) {
                failures.add(id);
            }
        }

        assertTrue(failures.isEmpty(),
                failures.size() + " of " + ids.size() + " invalidated sessions were reported valid, e.g. "
                        + failures.stream().limit(5).toList());
    }
}
