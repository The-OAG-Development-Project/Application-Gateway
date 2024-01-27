package org.owasp.oag.services.keymgm;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a local keystore for RSA keys used to sign JWT.
 * Expired keys will be automatically removed after a grace period of 10 minutes.
 */
@Component
public class LocalRsaJwkStore implements JwkStore {
    private static final Logger log = LoggerFactory.getLogger(LocalRsaJwkStore.class);

    private final MainConfig config;

    // for ease of use we use two maps to maintain the known keys. book-keeping is guaranteed by synchronizing access.
    private final Map<String, JWK> availableKeys = new HashMap<>();
    private final Map<String, Long> keyExpiry = new HashMap<>();
    // to cleanup expired keys
    private final ThreadPoolTaskScheduler scheduler;

    @Autowired
    public LocalRsaJwkStore(MainConfig config, @Qualifier("cleanupScheduler") ThreadPoolTaskScheduler scheduler) {
        if (config == null || scheduler == null) {
            throw new ConsistencyException("Null not allowed for constructor arguments of LocalRsaJwkStore.", null);
        }
        this.config = config;
        this.scheduler = scheduler;
        startScheduler();
    }

    @Override
    public synchronized JWKSet getSigningPublicKey(String kid) {
        if (kid == null) {
            return new JWKSet();
        }
        JWK key = availableKeys.get(kid);
        if (key != null) {
            return new JWKSet(key);
        } else {
            return new JWKSet();
        }
    }

    @Override
    public synchronized JWKSet getSigningPublicKeys() {
        return new JWKSet(Collections.unmodifiableList(new ArrayList<>(availableKeys.values())));
    }

    @Override
    public synchronized void addRawKey(String kid, Key key, long expiry) {
        if (kid == null || key == null) {
            throw new ApplicationException("kid and key may not be null when adding a JWT signing key.", null);
        }
        if (!(key instanceof RSAPublicKey)) {
            throw new ApplicationException("JWT signing key passed in must be of type RSAPublicKey. It was type " + key.getClass() + " instead.", null);
        }
        if (expiry < System.currentTimeMillis()) {
            LoggingUtils.contextual(() -> log.warn("Key with id {} added but it is already expired and will be removed in next cleanup cycle.", kid));
        }

        JWK jwk = new RSAKey((RSAPublicKey) key, KeyUse.SIGNATURE, null, Algorithm.parse("RS256"), kid, null, null, null, null, null, null, null, null);
        availableKeys.put(kid, jwk);
        keyExpiry.put(kid, expiry);
    }

    @Override
    public synchronized void addJwk(JWK key, long expiry) {
        if (key == null) {
            throw new IllegalArgumentException("JWK may not be null when adding a key.");
        }
        if (expiry < System.currentTimeMillis()) {
            LoggingUtils.contextual(() -> log.warn("Key with id {} added but it is already expired and will be removed in one of the next cleanup cycles.", key.getKeyID()));
        }

        availableKeys.put(key.getKeyID(), key);
        keyExpiry.put(key.getKeyID(), expiry);
    }

    @Override
    public synchronized void removeKey(String kid) {
        keyExpiry.remove(kid);
        availableKeys.remove(kid);
    }

    // start a scheduled task/timer with the key cleanup period defined.
    private void startScheduler() {
        try {
            scheduler.schedule(new CleanupKeysTask(this), Instant.ofEpochMilli(System.currentTimeMillis() + config.getKeyManagementProfile().getKeyRotationProfile().getCleanupFrequencySeconds() * 1000));
        } catch (Exception e) {
            throw new ConsistencyException("Could not start scheduler for key cleanup.", e);
        }
    }

    private synchronized void cleanupOldKeys() {
        LoggingUtils.contextual(() -> log.debug("Running signing key cleanup cycle on store {}.", this));

        long gracePeriodMillis = Duration.ofMinutes(10L).toMillis(); // 10 minutes
        long currentTime = System.currentTimeMillis() - gracePeriodMillis;
        @SuppressWarnings("Convert2MethodRef") List<String> expiredKid = keyExpiry.entrySet().stream()
                .filter(entry -> entry.getValue() < currentTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        expiredKid.forEach(kid -> {
            availableKeys.remove(kid);
            keyExpiry.remove(kid);
            LoggingUtils.contextual(() -> log.info("Signing key {} is expired. Removed it from local key store.", kid));
        });
    }

    private class CleanupKeysTask implements Runnable {
        private final LocalRsaJwkStore store;

        private CleanupKeysTask(LocalRsaJwkStore store) {
            this.store = store;
        }

        @Override
        public void run() {
            try {
                store.cleanupOldKeys();
            } catch (Exception e) {
                LoggingUtils.contextual(() -> log.error("Could not cleanup old signing keys. Consider restarting the service if this happens for a longer time period.", e));
            } finally {
                startScheduler();
            }
        }
    }
}
