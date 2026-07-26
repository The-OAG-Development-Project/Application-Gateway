package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.owasp.oag.integration.testInfrastructure.AbstractServerTest;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestClientFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

/**
 * Integration test for the default configuration of OWASP Application Gateway.
 * This test verifies that the application starts successfully with the sample
 * configuration and can handle basic HTTP requests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true", "logging.level.org.owasp.oag=TRACE"})
public class DefaultConfigurationTest extends AbstractServerTest {

    /**
     * Tests that OAG starts with the sample configuration and
     * delivers ok when accessed.
     * This test verifies the basic functionality of the gateway by sending a GET request
     * to the root path and expecting a 200 OK response.
     */
    @Test
    public void testStartWithSampleConfiguration() {
        webClient = TestClientFactory.createClient(serverPort, Duration.ofSeconds(20));
        webClient.get().uri("/").exchange().expectStatus().isOk();
    }
}
