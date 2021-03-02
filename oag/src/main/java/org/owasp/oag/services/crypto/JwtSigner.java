package org.owasp.oag.services.crypto;

import com.nimbusds.jwt.JWTClaimsSet;

public interface JwtSigner {

    String signJwt(JWTClaimsSet claimsSet);
}
