package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * Integration test for the default configuration of OWASP Application Gateway.
 * This test verifies that the application starts successfully with the sample
 * configuration and can handle basic HTTP requests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true", "logging.level.org.owasp.oag=TRACE"})
@AutoConfigureWebTestClient
public class DefaultConfigurationTest {

    /**
     * Web test client for making HTTP requests to the application.
     */
    @Autowired
    protected WebTestClient webClient;

    /**
     * Tests that OAG starts with the sample configuration and
     * delivers ok when accessed.
     * This test verifies the basic functionality of the gateway by sending a GET request
     * to the root path and expecting a 200 OK response.
     */
    @Test
    public void testStartWithSampleConfiguration() {
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(20)).build();
        webClient.get().uri("/").exchange().expectStatus().isOk();
    }
}
