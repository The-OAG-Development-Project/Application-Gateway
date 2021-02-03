package ch.gianlucafrei.nellygateway.reactiveMockServer;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This test test if the gateway correctly transforms the cookie into a JWT token and attaches it to the server
 */
public class UpstreamAuthenticationTest extends WiremockTest {

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }

    @Test
    void testUpstreamHeaderAnonymous() {

        stubFor(get("/testHeaders").willReturn(aResponse().withStatus(200)));

        webClient.get().uri("/testHeaders")
                .exchange()
                .expectStatus().isOk();

        verify(getRequestedFor(urlEqualTo("/testHeaders"))
                .withHeader("X-PROXY", equalTo("Nellygateway"))
                .withHeader("X-NELLY-ApiKey", equalTo(config.getNellyApiKey()))
                .withHeader("X-NELLY-Status", equalTo(SessionInformation.SESSION_STATE_ANONYMOUS)));
    }

    @Test
    void testUpstreamHeaderAuthenticated() {

        // Arrange
        var loginResult = makeLogin();

        stubFor(get("/testHeaders").willReturn(aResponse().withStatus(200)));

        webClient.get().uri("/testHeaders")
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .exchange()
                .expectStatus().isOk();

        verify(getRequestedFor(urlEqualTo("/testHeaders"))
                .withHeader("X-PROXY", equalTo("Nellygateway"))
                .withHeader("X-NELLY-ApiKey", equalTo(config.getNellyApiKey()))
                .withHeader("X-NELLY-Status", equalTo(SessionInformation.SESSION_STATE_AUTHENTICATED)));
    }

}
