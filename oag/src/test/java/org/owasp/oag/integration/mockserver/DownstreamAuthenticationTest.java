package org.owasp.oag.integration.mockserver;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.filters.proxy.DownstreamHeaderFilter;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This test test if the gateway correctly transforms the cookie into a JWT token and attaches it to the server
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.ch.gianlucafrei=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
public class DownstreamAuthenticationTest extends WiremockTest {

    @Test
    void testDownstreamHeaderAnonymous() {

        stubFor(get("/testHeaders").willReturn(aResponse().withStatus(200)));

        webClient.get().uri("/testHeaders")
                .exchange()
                .expectStatus().isOk();

        verify(getRequestedFor(urlEqualTo("/testHeaders"))
                .withHeader(DownstreamHeaderFilter.X_PROXY, equalTo(DownstreamHeaderFilter.X_PROXY_VALUE))
                .withHeader(DownstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_ANONYMOUS)));
    }

    @Test
    void testDownstreamHeaderAuthenticated() {

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

    @Test
    void testDownstreamAuthenticationContainsNoOAGCookies(){

        // Arrange
        var loginResult = makeLogin();

        stubFor(get("/testHeaders").willReturn(aResponse().withStatus(200)));

        webClient.get().uri("/testHeaders")
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .cookie("custom", "foo1")
                .cookie("custom", "foo2") // two cookies with same name
                .exchange()
                .expectStatus().isOk();

        WireMock.
        verify(getRequestedFor(urlEqualTo("/testHeaders"))
                .withCookie(LoginCookie.NAME, absent())
                .withCookie(LoginStateCookie.NAME, absent())
                .withCookie(CsrfCookie.NAME, absent())
                .withCookie("custom", equalTo("foo1"))
                .withCookie("custom", equalTo("foo2"))
                .withHeader(DownstreamHeaderFilter.X_OAG_STATUS, equalTo(SessionInformation.SESSION_STATE_AUTHENTICATED)));
    }
}
