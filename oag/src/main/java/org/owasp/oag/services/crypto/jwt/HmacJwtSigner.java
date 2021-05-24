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

    protected JWSSigner signer;
    protected JWSAlgorithm algorithm;
    protected String keyId;

    /**
     * Creates a new instance of a hmac signer.
     *
     * @param hexEncodedKey hex encoded string of 256, 384, 512 which is used as shared key
     * @param keyId         Optional keyID String which is added to the jwt header as kid
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

    @Override
    public boolean supportsJku() {
        return false;
    }

    @Override
    public URI getJku() {
        throw new ConsistencyException("Hmac Signer does not support jku header. Method should not be called. Use supportsJku to test.");
    }

    @Override
    protected String getKeyId() {
        return keyId;
    }

    @Override
    protected JWSAlgorithm getSigningAlgorithm() {
        return algorithm;
    }

    @Override
    protected JWSSigner getJwtSigner() {
        return signer;
    }
}
