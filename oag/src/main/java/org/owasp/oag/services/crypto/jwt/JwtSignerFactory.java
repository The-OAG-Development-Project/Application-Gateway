package org.owasp.oag.services.crypto.jwt;

import org.owasp.oag.extension.TypedExtension;

import java.util.Map;

/**
 * Factory class for jwt singer objects.
 * The factory is selected through the {@code signatureImplementation} setting of a JWT user mapping,
 * see {@link #type()}.
 */
public interface JwtSignerFactory extends TypedExtension {

    /**
     * Should return a instance of a jwt signer object
     *
     * @param hostUri  The host Uri
     * @param settings A map of settings from the configuration file
     * @return A jwt signer object
     */
    JwtSigner create(String hostUri, Map<String, Object> settings);
}
