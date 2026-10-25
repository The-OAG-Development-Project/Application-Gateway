package org.owasp.oag.services.keymgm;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConsistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Default mechanism to rotate keys: generates a new signing key with the configured {@link KeyGenerator},
 * publishes it in the configured {@link JwkStore} and repeats this in the configured interval.
 */
@Component
public class DefaultKeyRotation implements KeyRotation {

    /**
     * The {@code keyRotationProfile.type} value handled by this key rotation.
     */
    public static final String TYPE = "defaultKeyRotation";

    private static final Logger log = LoggerFactory.getLogger(DefaultKeyRotation.class);

    private final MainConfig config;
    private final JwkStoreRegistry jwkStores;
    private final KeyGeneratorRegistry keyGenerators;
    private final CurrentSigningKeyHolder sigKeyHolder;
    private final ThreadPoolTaskScheduler scheduler;

    /**
     * Constructs a DefaultKeyRotation instance. The rotation begins with {@link #start()}.
     *
     * @param config The main configuration for the application
     * @param jwkStores Registry of the JWK stores
     * @param keyGenerators Registry of the key generators
     * @param sigKeyHolder Holder for the current signing key
     * @param scheduler Scheduler for key rotation tasks
     */
    @Autowired
    public DefaultKeyRotation(MainConfig config, JwkStoreRegistry jwkStores, KeyGeneratorRegistry keyGenerators, CurrentSigningKeyHolder sigKeyHolder, @Qualifier("keyRotationScheduler") ThreadPoolTaskScheduler scheduler) {
        this.config = config;
        this.jwkStores = jwkStores;
        this.keyGenerators = keyGenerators;
        this.sigKeyHolder = sigKeyHolder;
        this.scheduler = scheduler;
    }

    @Override
    public void start() {
        generateNewKey();
        startScheduler();
    }

    // start a scheduled task/timer with the renewal period defined.
    private void startScheduler() {
        try {
            if (config.getKeyManagementProfile().getKeyRotationProfile().getUseSigningKeyRotation()) {
                scheduler.schedule(new RotateKeyTask(), Instant.ofEpochMilli(System.currentTimeMillis() + config.getKeyManagementProfile().getKeyRotationProfile().getSigningKeyRotationSeconds() * 1000));
            } else {
                scheduler.schedule(new RetrySchedulingTask(), Instant.ofEpochMilli(System.currentTimeMillis() + Duration.ofHours(1L).toMillis())); // retry scheduling, maybe config changed in the mean time
            }
        } catch (Exception e) {
            throw new ConsistencyException("Could not start scheduler of signing key rotation.", e);
        }
    }

    // synchronized to keep the order of the statements
    private synchronized void generateNewKey() {
        KeyGenerator.GeneratedKey newKey = keyGenerators.get(config.getKeyManagementProfile().getKeyGeneratorProfile().getType()).generateJWTSigningKey();
        String kid = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis();
        if (config.getKeyManagementProfile().getKeyRotationProfile().getUseSigningKeyRotation()) {
            expiry += (config.getKeyManagementProfile().getKeyRotationProfile().getSigningKeyRotationSeconds() + 600L) * 1000L;
        } else {
            expiry = Integer.MAX_VALUE;
        }
        jwkStores.get(config.getKeyManagementProfile().getJwkStoreProfile().getType()).addRawKey(kid, newKey.getKeyUsedToVerify(), expiry);
        sigKeyHolder.setCurrentSigningKey(kid, newKey);
        log.info("Signing key rotated. New signing key has kid {}.", kid);
    }

    private class RotateKeyTask implements Runnable {

        private RotateKeyTask() {
        }

        @Override
        public void run() {
            try {
                generateNewKey();
            } catch (Exception e) {
                log.error("Could not generate new signing key (key rotation failed). Consider restarting the service if this happens for a longer time period.", e);
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

    @Override
    public String type() {
        return TYPE;
    }
}
