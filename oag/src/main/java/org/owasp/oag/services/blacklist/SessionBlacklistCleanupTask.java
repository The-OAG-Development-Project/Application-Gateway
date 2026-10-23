package org.owasp.oag.services.blacklist;

import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Periodically prunes expired entries from the session blacklist.
 * <p>
 * Without this the blacklist only prunes at startup, so both the persisted file and the in-memory map
 * grow with every logout for the lifetime of the process.
 */
@Component
public class SessionBlacklistCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SessionBlacklistCleanupTask.class);

    private final SessionBlacklist sessionBlacklist;

    /**
     * Schedules the periodic cleanup.
     *
     * @param sessionBlacklist The blacklist to prune
     * @param config           Main configuration, read for the session duration
     * @param scheduler        The shared cleanup scheduler
     */
    @Autowired
    public SessionBlacklistCleanupTask(SessionBlacklist sessionBlacklist, MainConfig config,
                                       @Qualifier("cleanupScheduler") ThreadPoolTaskScheduler scheduler) {

        this.sessionBlacklist = sessionBlacklist;

        // An entry's ttl never exceeds one session duration, so running at twice that bounds how long
        // an expired entry lingers without introducing a separate tuning knob.
        var interval = Duration.ofSeconds(2L * config.getSessionBehaviour().getSessionDuration());

        scheduler.scheduleAtFixedRate(this::cleanup, interval);
        log.info("Scheduled session blacklist cleanup every {} seconds", interval.toSeconds());
    }

    /**
     * Runs one cleanup pass, swallowing any failure so the fixed-rate schedule is not cancelled.
     */
    void cleanup() {

        try {
            sessionBlacklist.cleanup();
            log.debug("Session blacklist cleanup completed");
        } catch (Exception e) {
            log.error("Session blacklist cleanup failed. Expired entries will accumulate until this succeeds.", e);
        }
    }
}
