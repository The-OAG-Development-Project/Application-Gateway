package org.owasp.oag.services.keymgm;

import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * Starts the configured {@link KeyRotation} once all singletons are created, before the gateway serves requests.
 */
@Component
public class KeyRotationStarter implements SmartInitializingSingleton {

    private final MainConfig config;
    private final KeyRotationRegistry registry;

    /**
     * Creates the starter.
     *
     * @param config   The main configuration
     * @param registry The key rotation registry
     */
    public KeyRotationStarter(MainConfig config, KeyRotationRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    /**
     * Starts the key rotation selected by {@code keyManagementProfile.keyRotationProfile.type}.
     *
     * @throws org.owasp.oag.exception.ConfigurationException If the type is unknown
     */
    @Override
    public void afterSingletonsInstantiated() {
        registry.get(config.getKeyManagementProfile().getKeyRotationProfile().getType()).start();
    }
}
