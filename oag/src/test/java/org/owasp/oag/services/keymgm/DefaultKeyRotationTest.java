package org.owasp.oag.services.keymgm;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, DefaultKeyRotationTest.PathTestConfig.class})
public class DefaultKeyRotationTest extends WiremockTest {

    @Autowired
    private CurrentSigningKeyHolder signingKeyHolder;

    @TestConfiguration
    public static class PathTestConfig {
        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/defaultKeyRotationConfiguration.yaml");
        }
    }

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
    }
}