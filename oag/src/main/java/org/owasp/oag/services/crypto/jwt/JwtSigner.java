package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.owasp.oag.exception.ConsistencyException;

import java.net.URI;

/**
 * A jwt signer is responsible for creating a signature for a jwt claims set.
 * This also includes all key handling functionality.
 * <p>
 * It is used by the jwt-user-mapping to create downstream tokens.
 */
public abstract class JwtSigner {
    /**
     * @return true when this signer supports the jku claim as specified in https://tools.ietf.org/html/rfc7515.
     */
    public abstract boolean supportsJku();

    /**
     * @return the jku url used by this signer.
     */
    public abstract URI getJku();

    /**
     * @return the keyId of the signing key or null if there is none.
     */
    protected abstract String getKeyId();

    /**
     * @return the signing algorithm to use for signing the JWT
     */
    protected abstract JWSAlgorithm getSigningAlgorithm();

    /**
     * @return the signer to use for creating the signature.
     */
    protected abstract JWSSigner getJwtSigner();

    /**
     * Creates the JWT and adds the passed in claims and signs it.
     * this is a template method.
     *
     * @param claimsSet the claims to add to the JWT
     * @return The signed JWT in Base64 encoded format.
     */
    public String createSignedJwt(JWTClaimsSet claimsSet) {
        JWSHeader jwsHeader = createJwsHeader();

        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);

        try {
            signedJWT.sign(getJwtSigner());
        } catch (JOSEException e) {
            throw new ConsistencyException("Could not sign jwt", e);
        }

        String jwt = signedJWT.serialize();
        return jwt;
    }

    private JWSHeader createJwsHeader() {
        var builder = new JWSHeader.Builder(getSigningAlgorithm()).type(JOSEObjectType.JWT);

        if (getKeyId() != null)
            builder = builder.keyID(getKeyId());

        if (supportsJku()) {
            // add jku claim when wished (default for asymmetric algos)
            builder.jwkURL(getJku());
        }

        return builder.build();
    }
}
