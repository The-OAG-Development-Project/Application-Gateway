package ch.gianlucafrei.nellygateway.integration.mockserver;

import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This test test if the gateway correctly transforms the cookie into a JWT token and attaches it to the server
 */
public class UpstreamAuthenticationTest extends WiremockTest {

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
