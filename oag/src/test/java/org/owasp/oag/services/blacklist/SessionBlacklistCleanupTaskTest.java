package org.owasp.oag.services.blacklist;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SessionBehaviour;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies how the session blacklist cleanup is scheduled.
 * <p>
 * Two properties matter. The interval must be derived from the configured session duration rather than
 * hardcoded, because an entry's ttl never exceeds one session duration. And the scheduled runnable must
 * never let an exception escape: a fixed-rate task that throws is cancelled permanently by
 * {@link java.util.concurrent.ScheduledExecutorService}, which would silently restore the unbounded
 * growth this task exists to prevent.
 */
class SessionBlacklistCleanupTaskTest {

    /**
     * A {@link ThreadPoolTaskScheduler} that records the fixed-rate registration instead of running it.
     */
    private static class CapturingScheduler extends ThreadPoolTaskScheduler {

        /** The interval the task registered itself with. */
        private Duration capturedInterval;

        /** The runnable the task registered. */
        private Runnable capturedTask;

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            this.capturedTask = task;
            this.capturedInterval = period;
            return null;
        }
    }

    /**
     * A blacklist whose cleanup counts its invocations and optionally fails.
     */
    private static class StubSessionBlacklist implements SessionBlacklist {

        /** Number of times cleanup was called. */
        private int cleanupCalls;

        /** When set, cleanup throws instead of succeeding. */
        private boolean failCleanup;

        @Override
        public void invalidateSession(String identifier, int ttl) {
            // not used by this test
        }

        @Override
        public boolean isInvalidated(String identifier) {
            return false;
        }

        @Override
        public void cleanup() {
            cleanupCalls++;
            if (failCleanup) {
                throw new IllegalStateException("cleanup failed");
            }
        }

        @Override
        public void close() throws IOException {
            // nothing to release
        }
    }

    private static MainConfig configWithSessionDuration(int sessionDuration) {

        var sessionBehaviour = new SessionBehaviour(sessionDuration, 60, "/", "/", "/");
        var config = new MainConfig();
        ReflectionTestUtils.setField(config, "sessionBehaviour", sessionBehaviour);
        return config;
    }

    @Test
    void schedulesCleanupAtTwiceTheConfiguredSessionDuration() {

        var scheduler = new CapturingScheduler();

        new SessionBlacklistCleanupTask(new StubSessionBlacklist(), configWithSessionDuration(900), scheduler);

        assertNotNull(scheduler.capturedTask, "the cleanup must be registered with the shared cleanup scheduler");
        assertEquals(Duration.ofSeconds(1800), scheduler.capturedInterval,
                "the interval must be twice the configured session duration");
    }

    @Test
    void derivesTheIntervalFromConfigurationRatherThanUsingAFixedValue() {

        var scheduler = new CapturingScheduler();

        new SessionBlacklistCleanupTask(new StubSessionBlacklist(), configWithSessionDuration(120), scheduler);

        assertEquals(Duration.ofSeconds(240), scheduler.capturedInterval,
                "a different session duration must yield a different interval");
    }

    @Test
    void aFailingCleanupDoesNotEscapeTheScheduledRunnable() {

        var scheduler = new CapturingScheduler();
        var blacklist = new StubSessionBlacklist();
        blacklist.failCleanup = true;

        new SessionBlacklistCleanupTask(blacklist, configWithSessionDuration(900), scheduler);

        // A fixed-rate task that throws is never run again, so the wrapper must swallow the failure.
        assertDoesNotThrow(() -> scheduler.capturedTask.run(),
                "a failing cleanup must not propagate out of the scheduled runnable");
        assertEquals(1, blacklist.cleanupCalls, "the scheduled runnable must delegate to the blacklist");
    }

    @Test
    void theScheduledRunnableDelegatesToTheBlacklist() {

        var scheduler = new CapturingScheduler();
        var blacklist = new StubSessionBlacklist();

        new SessionBlacklistCleanupTask(blacklist, configWithSessionDuration(900), scheduler);

        scheduler.capturedTask.run();
        scheduler.capturedTask.run();

        assertEquals(2, blacklist.cleanupCalls);
    }
}
