package org.owasp.oag.services.keymgm;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.owasp.oag.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a local keystore for RSA keys used to sign JWT.
 */
@Component("localRsaJwkStore")
public class LocalRsaJwkStore implements JwkStore {
    private static final Logger log = LoggerFactory.getLogger(LocalRsaJwkStore.class);

    // for ease of use we use two maps to maintain the known keys. It is perfectly acceptable that the two maps have
    // not the same kid all the time, eventual consistency is good enough.
    Map<String, JWK> availableKeys = Collections.synchronizedMap(new HashMap<>());
    Map<String, Long> keyExpiry = Collections.synchronizedMap(new HashMap<>());

    @Override
    public JWKSet getSigningPublicKey(String kid) {
        if (kid == null) {
            return new JWKSet();
        }
        JWK key = availableKeys.get(kid);
        if (key != null) {
            return new JWKSet(key);
        } else {
            return new JWKSet();
        }
    }

    @Override
    public JWKSet getSigningPublicKeys() {
        return new JWKSet(Collections.unmodifiableList(new ArrayList<>(availableKeys.values())));
    }

    @Override
    public void addRawKey(String kid, Key key, long expiry) {
        if (kid == null || key == null) {
            throw new ApplicationException("kid and key may not be null when adding a JWT signing key.", null);
        }
        if (!(key instanceof RSAPublicKey)) {
            throw new ApplicationException("JWT signing key passed in must be of type RSAPublicKey. It was type " + key.getClass() + " instead.", null);
        }
        if (expiry < System.currentTimeMillis()) {
            log.warn("Key with id {} added but it is already expired and will be removed in next cleanup cycle.", kid);
        }

        JWK jwk = new RSAKey((RSAPublicKey) key, KeyUse.SIGNATURE, null, Algorithm.parse("RS256"), kid, null, null, null, null, null);
        availableKeys.put(kid, jwk);
        keyExpiry.put(kid, expiry);
    }

    @Override
    public void addJwk(JWK key, long expiry) {
        if (key == null) {
            throw new IllegalArgumentException("JWK may not be null when adding a key.");
        }
        if (expiry < System.currentTimeMillis()) {
            log.warn("Key with id {} added but it is already expired and will be removed in next cleanup cycle.", key.getKeyID());
        }

        availableKeys.put(key.getKeyID(), key);
        keyExpiry.put(key.getKeyID(), expiry);
    }

    @Override
    public void removeKey(String kid) {
        keyExpiry.remove(kid);
        availableKeys.remove(kid);
    }
}
