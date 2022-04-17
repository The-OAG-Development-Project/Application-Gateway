package org.owasp.oag.services.keymgm;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Implements a key generator based on RSA.
 */
@Component
public class RsaKeyGenerator implements KeyGenerator {

    private final MainConfig config;

    @Autowired
    public RsaKeyGenerator(MainConfig config) {
        this.config = config;
        // verify key size is configured, else fails at bean creation time. Note: do not store keySize, so that it can be changed at runtime if later required
        assert (config.getKeyManagementProfile().getKeyGeneratorProfile().getKeySize() > 0);
    }

    @Override
    public GeneratedKey generateJWTSigningKey() {
        int keySize = config.getKeyManagementProfile().getKeyGeneratorProfile().getKeySize();
        try {
            KeyPairGenerator factory = KeyPairGenerator.getInstance("RSA");
            factory.initialize(keySize);
            KeyPair newPair = factory.generateKeyPair();
            return new GeneratedKey(newPair);
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigurationException("Configured keySize in KeyGeneratorProfile of MainConfig is not working for algorithm RSA. Configured size: " + keySize, e);
        }

    }
}
