package org.owasp.oag.services.crypto.jwt;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link JwtSignerFactory} implementations, keyed by signature implementation.
 */
@Component
public class JwtSignerRegistry extends TypeRegistry<JwtSignerFactory> {

    /**
     * Creates the registry.
     *
     * @param factories All JWT signer factories
     */
    public JwtSignerRegistry(List<JwtSignerFactory> factories) {
        super("JWT signer", factories);
    }
}
