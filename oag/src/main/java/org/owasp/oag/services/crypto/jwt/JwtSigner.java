package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * A jwt signer is responsible for creating a signature for a jwt claims set.
 * This also includes all key handling functionality.
 *
 * It is used by the jwt-user-mapping to create downstream tokens.
 */
public interface JwtSigner {

    String signJwt(JWTClaimsSet claimsSet);
}
