package org.owasp.oag.services.crypto.jwt;

import java.util.Map;

/**
 * Factory class for jwt singer objects.
 */
public interface JwtSignerFactory {

    /**
     * Bean name postfix for beans of this class.
     */
    static final String JWT_SIGNER_FACTORY_BEAN_POSTFIX = "-jwt-signer-factory";

    /**
     * Should return a instance of a jwt signer object
     * @param settings A map of settings from the configuration file
     * @return A jwt signer object
     */
    JwtSigner create(Map<String, Object> settings);
}
