package org.owasp.oag.services.keymgm;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link KeyRotation} implementations, keyed by {@code keyRotationProfile.type}.
 */
@Component
public class KeyRotationRegistry extends TypeRegistry<KeyRotation> {

    /**
     * Creates the registry.
     *
     * @param rotations All key rotations
     */
    public KeyRotationRegistry(List<KeyRotation> rotations) {
        super("key rotation", rotations);
    }
}
