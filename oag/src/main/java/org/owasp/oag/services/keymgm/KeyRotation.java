package org.owasp.oag.services.keymgm;

import org.owasp.oag.extension.TypedExtension;

/**
 * Rotates the JWT signing keys of OAG.
 * <p>
 * The implementation is selected through {@code keyManagementProfile.keyRotationProfile.type}, see {@link #type()}.
 * Only the selected implementation is started.
 */
public interface KeyRotation extends TypedExtension {

    /**
     * Generates the first signing key, assigns it in the {@link CurrentSigningKeyHolder} and starts the rotation.
     * Called once at startup, before the gateway serves requests.
     */
    void start();
}
