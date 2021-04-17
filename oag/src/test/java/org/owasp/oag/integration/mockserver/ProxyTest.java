package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.ch.gianlucafrei=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
public class ProxyTest extends WiremockTest {

    @Test
    public void testHttpProxy() {

        webClient
                .get().uri(TEST_1_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(TEST_1_RESPONSE);

        webClient
                .get().uri(TEST_2_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(TEST_2_RESPONSE);

        webClient
                .get().uri(TEST_NOTFOUND)
                .exchange()
                .expectStatus().isEqualTo(404);
    }
}
