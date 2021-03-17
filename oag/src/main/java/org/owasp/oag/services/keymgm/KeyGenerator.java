package org.owasp.oag.services.keymgm;

import java.security.Key;
import java.security.KeyPair;

/**
 * Generates new keys that can be used for signing JWT.
 */
public interface KeyGenerator {

    GeneratedKey generateJWTSigningKey();

    class GeneratedKey {
        public final KeyPair keyPair;
        public final Key key;

        GeneratedKey(Key key) {
            keyPair = null;
            this.key = key;
        }

        GeneratedKey(KeyPair pair) {
            keyPair = pair;
            this.key = null;
        }
    }
}
