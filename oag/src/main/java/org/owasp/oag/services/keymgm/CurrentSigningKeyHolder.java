package org.owasp.oag.services.keymgm;

import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PrivateKey;

/**
 * This component provides access to the current signing key of this OAG instance.
 */
@Component
public class CurrentSigningKeyHolder {
    private static final Logger log = LoggerFactory.getLogger(CurrentSigningKeyHolder.class);

    private KeyGenerator.GeneratedKey currentKey;
    private String kid;

    /**
     * @return the current private key for asymmetric encryption.
     */
    public synchronized PrivateKey getCurrentPrivateKey() {
        if (currentKey != null && currentKey.keyPair != null) {
            return currentKey.keyPair.getPrivate();
        }

        return null;
    }

    /**
     * @return the current symmetric key.
     */
    public synchronized Key getCurrentSymmetricKey() {
        if (currentKey != null && currentKey.key != null) {
            return currentKey.key;
        }

        return null;
    }

    /**
     * Sets the passed in key as new signing key and assigns its kid.
     *
     * @param kid           the kid of the new key
     * @param newSigningKey the new signing key for JWTs
     * @return the kid of the new signing key
     */
    public synchronized void setCurrentSigningKey(String kid, KeyGenerator.GeneratedKey newSigningKey) {
        if (newSigningKey == null) {
            throw new ConsistencyException("Signing key can not be set to null.", null);
        }
        if (currentKey != null && !currentKey.equalType(newSigningKey)) {
            LoggingUtils.contextual(() -> log.warn("New signing key {} has different type than the old signing key {}. This may lead to errors.", currentKey.getTypeString(), newSigningKey.getTypeString()));
        }
        currentKey = newSigningKey;
        this.kid = kid;
    }

    /**
     * @return the current signing keys kid
     */
    public synchronized String getKid() {
        return kid;
    }
}
