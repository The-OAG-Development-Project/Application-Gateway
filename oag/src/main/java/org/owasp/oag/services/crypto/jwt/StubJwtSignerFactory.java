package org.owasp.oag.services.crypto.jwt;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;

import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

@Component("stub"  + JWT_SIGNER_FACTORY_BEAN_POSTFIX)
public class StubJwtSignerFactory implements JwtSignerFactory {

    public JwtSigner create(Map<String, Object> settings){

        // Generate a new random in-memory key
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);

        String sharedKey = Hex.encodeHexString(sharedSecret);
        return new HmacJwtSigner(sharedKey, "stub key for testing, please configure your own");
    }
}
