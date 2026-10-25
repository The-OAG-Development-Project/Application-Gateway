package org.owasp.oag.services.keymgm;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link JwkStore} implementations, keyed by {@code jwkStoreProfile.type}.
 */
@Component
public class JwkStoreRegistry extends TypeRegistry<JwkStore> {

    /**
     * Creates the registry.
     *
     * @param stores All JWK stores
     */
    public JwkStoreRegistry(List<JwkStore> stores) {
        super("JWK store", stores);
    }
}
