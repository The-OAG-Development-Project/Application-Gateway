package org.owasp.oag.services.keymgm;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link KeyGenerator} implementations, keyed by {@code keyGeneratorProfile.type}.
 */
@Component
public class KeyGeneratorRegistry extends TypeRegistry<KeyGenerator> {

    /**
     * Creates the registry.
     *
     * @param generators All key generators
     */
    public KeyGeneratorRegistry(List<KeyGenerator> generators) {
        super("key generator", generators);
    }
}
