package ch.gianlucafrei.nellygateway.integration.mockserver;

import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


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
