package org.owasp.oag.services.keymgm;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.infrastructure.factories.KeyManagementFactory;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Default mechanism to rotate keys.
 * If you want to use another implementation, you may be so by wriiting your own key rotation mechanism.
 * Note that in that case you must make sure that when the bean is loaded:
 * - you generate a first signing key and assign it in the CurrentSigningKeyHolder
 */
@Component("defaultKeyRotation")
public class DefaultKeyRotation implements KeyRotation {

    private static final Logger log = LoggerFactory.getLogger(DefaultKeyRotation.class);

    private final MainConfig config;
    private final KeyManagementFactory keyMgmFactory;
    private final CurrentSigningKeyHolder sigKeyHolder;
    private final ThreadPoolTaskScheduler scheduler;

    @Autowired
    public DefaultKeyRotation(MainConfig config, KeyManagementFactory keyMgmFactory, CurrentSigningKeyHolder sigKeyHolder, @Qualifier("keyRotationScheduler") ThreadPoolTaskScheduler scheduler) {
        this.config = config;
        this.keyMgmFactory = keyMgmFactory;
        this.sigKeyHolder = sigKeyHolder;
        this.scheduler = scheduler;
        generateNewKey();
        startScheduler();
    }

    // start a scheduled task/timer with the renewal period defined.
    private void startScheduler() {
        try {
            if (config.getKeyManagementProfile().getKeyRotationProfile().getUseSigningKeyRotation()) {
                scheduler.schedule(new RotateKeyTask(), Instant.ofEpochMilli(System.currentTimeMillis() + config.getKeyManagementProfile().getKeyRotationProfile().getSigningKeyRotationSeconds() * 1000));
            } else {
                //noinspection PointlessArithmeticExpression
                scheduler.schedule(new RetrySchedulingTask(), Instant.ofEpochMilli(System.currentTimeMillis() + 1 * 60 * 60 * 1000)); // retry scheduling, maybe config changed in the mean time
            }
        } catch (Exception e) {
            throw new ConsistencyException("Could not start scheduler of signing key rotation.", e);
        }
    }

    // synchronized to keep the order of the statements
    private synchronized void generateNewKey() {
        KeyGenerator.GeneratedKey newKey = keyMgmFactory.getKeyGenerator().generateJWTSigningKey();
        String kid = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis();
        if (config.getKeyManagementProfile().getKeyRotationProfile().getUseSigningKeyRotation()) {
            expiry += (config.getKeyManagementProfile().getKeyRotationProfile().getSigningKeyRotationSeconds() + 600L) * 1000;
        } else {
            expiry = Integer.MAX_VALUE;
        }
        keyMgmFactory.getJWKStore().addRawKey(kid, newKey.getKeyUsedToVerify(), expiry);
        sigKeyHolder.setCurrentSigningKey(kid, newKey);
        LoggingUtils.contextual(() -> log.info("Signing key rotated. New signing key has kid {}.", kid));
    }

    private class RotateKeyTask implements Runnable {

        private RotateKeyTask() {
        }

        @Override
        public void run() {
            try {
                generateNewKey();
            } catch (Exception e) {
                LoggingUtils.contextual(() -> log.error("Could not generate new signing key (key rotation failed). Consider restarting the service if this happens for a longer time period.", e));
            } finally {
                startScheduler();
            }
        }
    }

    private class RetrySchedulingTask implements Runnable {

        private RetrySchedulingTask() {
        }

        @Override
        public void run() {
            startScheduler();
        }
    }
}
