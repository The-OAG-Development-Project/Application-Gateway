package org.owasp.oag.services.crypto.jwt;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;
import java.util.Map;

public class StubJwtSignerFactory implements JwtSignerFactory {

    public JwtSigner create(String hostUri, Map<String, Object> settings) {

        // Generate a new random in-memory key
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);

        String sharedKey = Hex.encodeHexString(sharedSecret);
        return new HmacJwtSigner(sharedKey, "stub key for testing, please configure your own");
    }
}
