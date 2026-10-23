package org.owasp.oag.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configures the task schedulers used for periodic key management work.
 */
@Configuration
public class SchedulerConfiguration {

    /**
     * Creates the scheduler that triggers signing key rotation.
     *
     * @return The configured thread pool task scheduler
     */
    @Bean(name = "keyRotationScheduler")
    public ThreadPoolTaskScheduler keyRotationThreadPoolTaskScheduler() {

        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("keyRotate");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Creates the scheduler that removes signing keys once they are no longer needed.
     *
     * @return The configured thread pool task scheduler
     */
    @Bean(name = "cleanupScheduler")
    public ThreadPoolTaskScheduler cleanupThreadPoolTaskScheduler() {

        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("keyCleanup");
        scheduler.initialize();
        return scheduler;
    }
}
