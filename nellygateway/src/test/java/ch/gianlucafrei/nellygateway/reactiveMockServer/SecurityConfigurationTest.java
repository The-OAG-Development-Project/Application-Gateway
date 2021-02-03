package ch.gianlucafrei.nellygateway.reactiveMockServer;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

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

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
