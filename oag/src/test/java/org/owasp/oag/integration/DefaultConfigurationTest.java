package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true", "logging.level.ch.gianlucafrei=TRACE"},
        classes = {IntegrationTestConfig.class})
public class DefaultConfigurationTest{

    @Autowired
    protected WebTestClient webClient;

    /**
     * Tests that OAG starts with the sample configuration and
     * delivers ok when accessed
     */
    @Test
    public void testStartWithSampleConfiguration(){

        webClient.get().uri("/").exchange().expectStatus().isOk();
    }
}
