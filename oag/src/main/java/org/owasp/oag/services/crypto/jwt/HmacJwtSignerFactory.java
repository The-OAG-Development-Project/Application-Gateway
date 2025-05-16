package org.owasp.oag.services.crypto.jwt;

import org.owasp.oag.exception.ConfigurationException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating HMAC-based JWT signers.
 * This factory creates JWT signers that use HMAC (Hash-based Message Authentication Code)
 * for signing JWTs with a shared secret key.
 */
@Component
public class HmacJwtSignerFactory implements JwtSignerFactory {

    /**
     * Creates a new HMAC-based JWT signer with the provided settings.
     * 
     * @param hostUri The URI of the host, used for identifying the token issuer
     * @param settings A map containing configuration settings, must include:
     *                 - "secretKey": A string containing the shared secret key for HMAC signing
     *                 - "keyId": A string identifier for the key
     * @return A new HmacJwtSigner instance
     * @throws ConfigurationException if required settings are missing or invalid
     */
    public JwtSigner create(String hostUri, Map<String, Object> settings) {

        var sharedKey = settings.getOrDefault("secretKey", null);
        var keyId = settings.getOrDefault("keyId", null);

        if (sharedKey == null)
            throw new ConfigurationException("HmacJwtSignerFactory Invalid configuration: 'secretKey' not configured", null);

        if (!(sharedKey instanceof String))
            throw new ConfigurationException("HmacJwtSignerFactory Invalid configuration: 'secretKey' must be a hex-string", null);

        if (!(keyId instanceof String))
            throw new ConfigurationException("HmacJwtSignerFactory Invalid configuration: 'keyId' must be a string", null);

        return new HmacJwtSigner((String) sharedKey, (String) keyId);
    }
}
