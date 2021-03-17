package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.JwkStoreProfile;
import org.owasp.oag.config.configuration.KeyGeneratorProfile;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.keymgm.JwkStore;
import org.owasp.oag.services.keymgm.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The only existing KeyManagementFactory implementation.
 */
@Component
public class DefaultKeyManagementFactory implements KeyManagementFactory {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private MainConfig config;

    @Override
    public JwkStore getJWKStore() {
        JwkStoreProfile profile = config.getKeyManagementProfile().getJwkStoreProfile();
        JwkStore implClass = context.getBean(profile.getType(), JwkStore.class);

        if (implClass == null) {
            throw new ConfigurationException("JwkStore implementation class not found: " + profile.getType(), null);
        }
        return implClass;
    }

    @Override
    public KeyGenerator getKeyGenerator() {
        KeyGeneratorProfile profile = config.getKeyManagementProfile().getKeyGeneratorProfile();
        KeyGenerator implClass = context.getBean(profile.getType(), KeyGenerator.class);

        if (implClass == null) {
            throw new ConfigurationException("KeyGenerator implementation class not found: " + profile.getType(), null);
        }
        return implClass;
    }
}
