package org.owasp.oag.services.keymgm;

import org.owasp.oag.exception.ConsistencyException;

import java.security.Key;
import java.security.KeyPair;

/**
 * Generates new keys that can be used for signing JWT.
 */
public interface KeyGenerator {

    /**
     * Generates a new JWT signing key.
     * 
     * @return a new JWT Signing Key of the type specified in the configuration.
     */
    GeneratedKey generateJWTSigningKey();

    /**
     * A Key used for Signing JWT. Can be a KeyPair -> asymmetric (e.g. RS/ES) signing or a Key -> symmetric key for signing (i.e. HS)
     */
    class GeneratedKey {
        /**
         * The asymmetric key pair, if this is an asymmetric key.
         */
        public final KeyPair keyPair;
        
        /**
         * The symmetric key, if this is a symmetric key.
         */
        public final Key key;

        /**
         * Constructs a new GeneratedKey with a symmetric key.
         * 
         * @param key The symmetric key
         * @throws ConsistencyException if the key is null
         */
        GeneratedKey(Key key) {
            if (key == null) {
                throw new ConsistencyException("Current signing Key may never be null.", null);
            }
            keyPair = null;
            this.key = key;
        }

        /**
         * Constructs a new GeneratedKey with an asymmetric key pair.
         * 
         * @param pair The asymmetric key pair
         * @throws ConsistencyException if the key pair is null
         */
        GeneratedKey(KeyPair pair) {
            if (pair == null) {
                throw new ConsistencyException("Current signing Key may never be null.", null);
            }
            keyPair = pair;
            this.key = null;
        }

        /**
         * Compares the type of this key with another key.
         * 
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
         * Gets the type of key as a string.
         * 
         * @return the type of key that is packed in the GeneratedKey class.
         */
        public String getTypeString() {
            if (keyPair != null) {
                return keyPair.getPublic().getClass().getName();
            }
            return key.getClass().getName();
        }

        /**
         * Gets the key used for signature verification.
         * 
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
