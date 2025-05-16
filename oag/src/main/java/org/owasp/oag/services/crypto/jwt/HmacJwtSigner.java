package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.exception.ConsistencyException;

import java.net.URI;

/**
 * Implementation of a HMAC jwt singer. Supports HS256, HS384 and HS512.
 * The sharedKey is passed via constructor parameter in hex format.
 */
public class HmacJwtSigner extends JwtSigner {

    /** The JWS signer implementation for HMAC signatures */
    protected JWSSigner signer;
    
    /** The HMAC algorithm to use (HS256, HS384, or HS512) */
    protected JWSAlgorithm algorithm;
    
    /** The key ID value to include in JWT headers */
    protected String keyId;

    /**
     * Creates a new instance of a hmac signer.
     *
     * @param hexEncodedKey hex encoded string of 256, 384, 512 which is used as shared key
     * @param keyId         Optional keyID String which is added to the jwt header as kid
     * @throws ConfigurationException if the key length is invalid or signer creation fails
     * @throws ApplicationException if the key cannot be decoded as hex
     */
    public HmacJwtSigner(String hexEncodedKey, String keyId) {

        try {

            byte[] sharedSecret = Hex.decodeHex(hexEncodedKey);

            switch (sharedSecret.length) {
                case 256 / 8:
                    algorithm = JWSAlgorithm.HS256;
                    break;
                case 385 / 8:
                    algorithm = JWSAlgorithm.HS384;
                    break;
                case 512 / 8:
                    algorithm = JWSAlgorithm.HS512;
                    break;
                default:
                    throw new ConfigurationException("Invalid key length: " + sharedSecret.length + " , must be 256, 384 or 512 bits", null);
            }

            try {
                signer = new MACSigner(sharedSecret);
            } catch (KeyLengthException e) {
                throw new ConfigurationException("Could not generate signing key", e);
            }

            this.keyId = keyId;

        } catch (DecoderException e) {
            throw new ApplicationException("Could not decode key. Must be a hex string of 256, 384 or 512 bits", e);
        }
    }

    /**
     * Indicates whether this signer supports the JKU header.
     * HMAC signers don't support JKU as the secret is shared directly.
     *
     * @return false as HMAC signers don't support JKU header
     */
    @Override
    public boolean supportsJku() {
        return false;
    }

    /**
     * Returns the JKU (JWK Set URL) for this signer.
     * Not supported for HMAC signers.
     *
     * @return never returns as method throws an exception
     * @throws ConsistencyException always, as HMAC signers don't support JKU
     */
    @Override
    public URI getJku() {
        throw new ConsistencyException("Hmac Signer does not support jku header. Method should not be called. Use supportsJku to test.");
    }

    /**
     * Returns the key ID associated with this signer.
     *
     * @return the key ID string used in JWT headers
     */
    @Override
    protected String getKeyId() {
        return keyId;
    }

    /**
     * Returns the JWS algorithm used by this signer.
     *
     * @return the HMAC algorithm (HS256, HS384, or HS512)
     */
    @Override
    protected JWSAlgorithm getSigningAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the underlying JWS signer implementation.
     *
     * @return the MACSigner instance used for signing
     */
    @Override
    protected JWSSigner getJwtSigner() {
        return signer;
    }
}
