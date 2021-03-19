package org.owasp.oag.services.crypto.jwt;

import java.util.Map;

public interface JwtSignerFactory {

    static final String JWT_SIGNER_FACTORY_BEAN_POSTFIX = "-jwt-signer-factory";
    JwtSigner create(Map<String, Object> settings);
}
