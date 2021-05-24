package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import org.owasp.oag.exception.ConsistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;

import static org.owasp.oag.utils.SharedConstants.JWKS_BASE_URI;

/**
 * Implements the RSA based JWT signatures.
 */
public class RsaJwtSigner extends JwtSigner {
    private static final Logger log = LoggerFactory.getLogger(RsaJwtSigner.class);

    private final RSASSASigner signer;
    private final String kid;
    private final JWSAlgorithm signingAlgo;
    private final URI jku;

    /**
     * @param signingKey  the key used to sign the JWT.
     * @param kid         The kid to use
     * @param signingAlgo a valid Rsa Signing Algo like RS256
     */
    public RsaJwtSigner(PrivateKey signingKey, String kid, JWSAlgorithm signingAlgo, String hostUri) {
        if (signingAlgo == null) {
            throw new ConsistencyException("Must provide a RSA signing algorithm, null is not allowed.");
        }
        if (kid == null) {
            throw new ConsistencyException("Must provide a kid, null is not allowed.");
        }
        if (hostUri == null) {
            throw new ConsistencyException("Must provide a hostUri, null is not allowed.");
        }

        this.signingAlgo = signingAlgo;
        this.kid = kid;
        try {
            this.jku = new URI(hostUri + JWKS_BASE_URI);
        } catch (URISyntaxException e) {
            throw new ConsistencyException("Could not create jku from provided data (invalid format): " + hostUri + JWKS_BASE_URI);
        }
        try {
            signer = new RSASSASigner(signingKey);
        } catch (Exception e) {
            throw new ConsistencyException("Could not create RSA signer due to " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsJku() {
        return true;
    }

    @Override
    public URI getJku() {
        return jku;
    }

    @Override
    protected String getKeyId() {
        return kid;
    }

    @Override
    protected JWSAlgorithm getSigningAlgorithm() {
        return signingAlgo;
    }

    @Override
    protected JWSSigner getJwtSigner() {
        return signer;
    }
}
