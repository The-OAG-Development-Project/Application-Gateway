package org.owasp.oag.services.keymgm;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import java.security.Key;

/**
 * Defines the interface for JWK based key management / storage.
 */
public interface JwkStore {

    /**
     * Gets the JWT signing pub-key with passed in kid.
     *
     * @param kid the key id of the key requested (taken from JWT header).
     * @return A JWK Set with 0 or 1 JWK.
     */
    JWKSet getSigningPublicKey(String kid);

    /**
     * @return A JWK Set containing all known (currently potentially valid) public keys that can be used to verify JWT signatures.
     */
    JWKSet getSigningPublicKeys();

    /**
     * Adds the passed in key as a known JWT signing verification key for the passed in kid.
     *
     * @param kid    the key id of the passed in key
     * @param key    the (new) key that can be used to verify JWT signatures mentioning the passed in kid
     * @param expiry the (unix) timestamp when the key can be disposed of as it is replaced by then
     */
    void addRawKey(String kid, Key key, long expiry);

    /**
     * Adds the passed in JWK to the list of known JWT signers.
     *
     * @param key    the JWK to add
     * @param expiry the (unix) timestamp when the key can be disposed of as it is replaced by then
     */
    void addJwk(JWK key, long expiry);

    /**
     * Deletes the key with the passed in kid from the list of known JWT signers
     *
     * @param kid the key id of the key that should be removed
     */
    void removeKey(String kid);
}
