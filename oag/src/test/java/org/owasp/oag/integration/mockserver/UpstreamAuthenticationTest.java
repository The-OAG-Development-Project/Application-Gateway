package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.filters.proxy.UpstreamHeaderFilter;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;

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
                .withHeader(UpstreamHeaderFilter.X_PROXY, equalTo(UpstreamHeaderFilter.X_PROXY_VALUE))
                .withHeader(UpstreamHeaderFilter.X_OAG_API_KEY, equalTo(config.getDownstreamApiKey()))
                .withHeader(UpstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_ANONYMOUS)));
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
                .withHeader(UpstreamHeaderFilter.X_PROXY, equalTo(UpstreamHeaderFilter.X_PROXY_VALUE))
                .withHeader(UpstreamHeaderFilter.X_OAG_API_KEY, equalTo(config.getDownstreamApiKey()))
                .withHeader(UpstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_AUTHENTICATED)));
    }

}
