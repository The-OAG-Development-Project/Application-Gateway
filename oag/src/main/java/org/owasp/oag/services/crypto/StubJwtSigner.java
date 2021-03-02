package org.owasp.oag.services.crypto;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 *  Mock implementation that maps the userModel into a JWT with a random hmac key
 *  Should only used during development as a stub for a useful implementation
 */
@Component("stub-jwt-signer")
public class StubJwtSigner implements JwtSigner {

    private JWSSigner signer;

    public StubJwtSigner() {

        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);

        try {
            signer = new MACSigner(sharedSecret);
        } catch (KeyLengthException e) {
            throw new RuntimeException("Could not generate signing key");
        }
    }

    @Override
    public String signJwt(JWTClaimsSet claimsSet) {
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException("Could not sign jwt");
        }

        String jwt = signedJWT.serialize();
        return jwt;
    }
}
