package org.owasp.oag.services.keymgm;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTest;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.services.crypto.jwt.JwtSigner;
import org.owasp.oag.services.crypto.jwt.JwtSignerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

public class RSAKeySignerTest extends IntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CurrentSigningKeyHolder signingKeyHolder;

    @Test
    public void testBasicSigning() throws ParseException {
        JwtSignerFactory factory = (JwtSignerFactory) context.getBean("rsa" + JWT_SIGNER_FACTORY_BEAN_POSTFIX);
        JwtSigner rsaSigner = factory.create("https://fake.oag.owasp.org", null);

        assertTrue(rsaSigner.supportsJku());

        var claimsBuilder = new JWTClaimsSet.Builder();
        claimsBuilder.subject("tst")
                .issuer("oag.test")
                .audience("oag.test")
                .issueTime(new Date())
                .notBeforeTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 10000))
                .jwtID("jti_24_1");

        // Sign claims set
        JWTClaimsSet claimsSet = claimsBuilder.build();

        String compactJwt = rsaSigner.createSignedJwt(claimsSet);

        String[] parts = compactJwt.split("\\.");
        assertEquals(3, parts.length);
        assertTrue(parts[2].length() > 20);

        JOSEObject jwt = JOSEObject.parse(compactJwt);
        assertNotNull(jwt);
        Header header = jwt.getHeader();
        assertNotNull(header);
        assertEquals(Algorithm.parse("RS256"), header.getAlgorithm());
        assertEquals(JOSEObjectType.JWT, header.getType());
        assertNotNull(((JWSHeader) header).getJWKURL());
        assertNotNull(((JWSHeader) header).getKeyID());
        assertTrue(((JWSHeader) header).getJWKURL().getPath().contains(".well-known"));
    }

    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {
        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/defaultKeyRotationConfiguration.yaml");
        }
    }
}
