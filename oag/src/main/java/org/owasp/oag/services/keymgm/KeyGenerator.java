package org.owasp.oag.services.keymgm;

import org.owasp.oag.exception.ConsistencyException;

import java.security.Key;
import java.security.KeyPair;

/**
 * Generates new keys that can be used for signing JWT.
 */
public interface KeyGenerator {

    /**
     * @return a new JWT Signing Key of the type specified in the configuration.
     */
    GeneratedKey generateJWTSigningKey();

    /**
     * A Key used for Signing JWT. Can be a KeyPair -> asymmetric (e.g. RS/ES) signing or a Key -> symmetric key for signing (i.e. HS)
     */
    class GeneratedKey {
        public final KeyPair keyPair;
        public final Key key;

        GeneratedKey(Key key) {
            if (key == null) {
                throw new ConsistencyException("Current signing Key may never be null.", null);
            }
            keyPair = null;
            this.key = key;
        }

        GeneratedKey(KeyPair pair) {
            if (pair == null) {
                throw new ConsistencyException("Current signing Key may never be null.", null);
            }
            keyPair = pair;
            this.key = null;
        }

        /**
         * @param other the Key to compare
         * @return true when this object and the new signing key are keys of the same type.
         */
        public boolean equalType(GeneratedKey other) {
            if (other == null) {
                return false;
            }
            return getTypeString().equals(other.getTypeString());
        }

        /**
         * @return the type of key that is packed in the GeneratedKey class.
         */
        public String getTypeString() {
            if (keyPair != null) {
                return keyPair.getPublic().getClass().getName();
            }
            return key.getClass().getName();
        }

        /**
         * @return the Key used to verify the signature. This may be a symmetric key or a PublicKey.
         */
        public Key getKeyUsedToVerify() {
            if (keyPair != null) {
                return keyPair.getPublic();
            }
            return key;
        }
    }
}
