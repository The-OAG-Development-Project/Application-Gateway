package org.owasp.oag.services.keymgm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultKeyRotationTest extends WiremockTest {

    @Autowired
    private CurrentSigningKeyHolder signingKeyHolder;

    @Test
    void keyRotationTest() throws InterruptedException {
        // here the rotationImpl should be initialized and a valid key set
        assertNotNull(signingKeyHolder.getCurrentPrivateKey());
        assertNotNull(signingKeyHolder.getKid());
        String oldKid = signingKeyHolder.getKid();
        PrivateKey oldKey = signingKeyHolder.getCurrentPrivateKey();

        // we have to wait to let key rotation happen
        Thread.sleep(4000);

        assertNotEquals(oldKid, signingKeyHolder.getKid());
        assertNotEquals(oldKey, signingKeyHolder.getCurrentPrivateKey());

        //execute test, leads to error, circular bean reference issue with JWKSController, mainConfig, defaultKeyRotation...;

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