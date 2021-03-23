package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HmacJwtSignerTest {

    @Test
    public void testHmacJwtSigner() throws Exception {

        // Arrange
        String key = "DEADBEEFdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"; // hex string with mixed case
        String keyId = "KeyID";
        String subject = "John Doe";
        var signer = new HmacJwtSigner(key, keyId);
        var claims = new JWTClaimsSet.Builder().subject(subject).build();
        JWSVerifier verifier = new MACVerifier(Hex.decodeHex(key));

        // Act
        var jwt = signer.signJwt(claims);

        // Assert
        SignedJWT parsedJwt = SignedJWT.parse(jwt);
        assertEquals(keyId, parsedJwt.getHeader().getKeyID());
        assertEquals(subject, parsedJwt.getJWTClaimsSet().getSubject());
        assertEquals(JWSAlgorithm.HS256, parsedJwt.getHeader().getAlgorithm());
        assertTrue(parsedJwt.verify(verifier));
    }

    @Test
    public void testHmacJwtSignerInvalidParameters() throws Exception {

        assertThrows(Exception.class, () -> new HmacJwtSigner(null, null), "Null key should result in exception");
        assertThrows(Exception.class, () -> new HmacJwtSigner("DEADBEEF", null), "Invalid key length should result in exception");
        assertThrows(Exception.class, () -> new HmacJwtSigner("XXXXXXXXdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef", null), "Non hex string should result in exception");
    }
}