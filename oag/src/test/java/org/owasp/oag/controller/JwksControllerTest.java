package org.owasp.oag.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.integration.testInfrastructure.IntegrationTest;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.owasp.oag.utils.SharedConstants.JWKS_BASE_URI;

public class JwksControllerTest extends IntegrationTest {

    @Autowired
    protected WebTestClient webClient;
    @Autowired
    MainConfig mainConfig;

    @Test
    public void testJwksProvided() throws Exception {
        WebTestClient.ResponseSpec resp = webClient.get().uri(JWKS_BASE_URI).exchange();
        JWKSet jwks = JWKSet.load(new ByteArrayInputStream(resp.expectBody().returnResult().getResponseBody()));

        assertEquals(1, jwks.getKeys().size());
        assertEquals(KeyType.RSA, jwks.getKeys().get(0).getKeyType());
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
