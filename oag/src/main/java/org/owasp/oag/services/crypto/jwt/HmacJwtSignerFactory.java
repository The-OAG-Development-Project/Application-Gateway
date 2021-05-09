package org.owasp.oag.services.crypto.jwt;

import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

@Component("hmac" + JWT_SIGNER_FACTORY_BEAN_POSTFIX)
public class HmacJwtSignerFactory implements JwtSignerFactory {

    public JwtSigner create(Map<String, Object> settings){

        var sharedKey = settings.getOrDefault("secretKey", null);
        var keyId = settings.getOrDefault("keyId", null);

        if(sharedKey == null)
            throw new RuntimeException("HmacJwtSignerFactory Invalid configuration: 'secretKey' not configured");

        if(! (sharedKey instanceof String))
            throw new RuntimeException("HmacJwtSignerFactory Invalid configuration: 'secretKey' must be a hex-string");

        if(! (keyId instanceof String))
            throw new RuntimeException("HmacJwtSignerFactory Invalid configuration: 'keyId' must be a string");

        return new HmacJwtSigner((String) sharedKey, (String) keyId);
    }
}
