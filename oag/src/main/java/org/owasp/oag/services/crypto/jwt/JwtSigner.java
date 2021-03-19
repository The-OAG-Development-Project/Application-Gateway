package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jwt.JWTClaimsSet;

public interface JwtSigner {

    String signJwt(JWTClaimsSet claimsSet);
}
