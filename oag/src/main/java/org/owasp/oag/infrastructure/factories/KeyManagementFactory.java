package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.keymgm.JwkStore;
import org.owasp.oag.services.keymgm.KeyGenerator;

/**
 * Factory interface to create beans required in the area of key handling and key management.
 */
public interface KeyManagementFactory {

    /**
     * @return the configured JWKStore. See MainConfig, keyManagementProfile, jwkStoreProfile, type
     */
    JwkStore getJWKStore();

    /**
     * @return the configured KeyGenerator. See MainConfig, keyManagementProfile, keyGeneratorProfile, type
     */
    KeyGenerator getKeyGenerator();
}
