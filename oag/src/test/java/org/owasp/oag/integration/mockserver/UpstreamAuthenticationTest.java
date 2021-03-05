package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.filters.proxy.DownstreamHeaderFilter;
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
                .withHeader(DownstreamHeaderFilter.X_PROXY, equalTo(DownstreamHeaderFilter.X_PROXY_VALUE))
                .withHeader(DownstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_ANONYMOUS)));
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
                .withHeader(DownstreamHeaderFilter.X_PROXY, equalTo(DownstreamHeaderFilter.X_PROXY_VALUE))
                .withHeader(DownstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_AUTHENTICATED)));
    }

}
