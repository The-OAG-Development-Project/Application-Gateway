package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
class SecurityConfigurationTest extends WiremockTest {

    @Test
    void testProxyBlocksWhenAllowAnonymous() throws Exception {

        // Checks if allowAnonymous: yes works
        webClient.get().uri(TEST_1_ENDPOINT)
                .exchange().expectStatus().isOk();

        // Checks if allowAnonymous: no works
        webClient.get().uri("/secure" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testProxyAddsSecurityHeaders() throws Exception {

        stubFor(get(urlEqualTo("/securityHeader"))
                .willReturn(aResponse()
                        .withHeader("Server", "Secret Server")));

        // Makes a request through zuul and check if the security headers are applied
        webClient.get().uri("/securityHeader")
                .exchange()
                .expectHeader().doesNotExist("Server")
                .expectHeader().valueMatches("X-Frame-Options", "SAMEORIGIN")
                .expectHeader().valueMatches("X-Content-Type-Options", "nosniff");
    }

    @Test
    void testProxyBlocksNoneAllowedMethods() throws Exception {

        webClient.get().uri("/static" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isOk();

        webClient.delete().uri("/static" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isEqualTo(405);
    }
}
