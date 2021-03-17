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
@Component("rsaKeyGenerator")
public class RsaKeyGenerator implements KeyGenerator {

    private final int keySize;

    @Autowired
    public RsaKeyGenerator(MainConfig config) {
        keySize = config.getKeyManagementProfile().getKeyGeneratorProfile().getKeySize();
    }

    @Override
    public GeneratedKey generateJWTSigningKey() {
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
