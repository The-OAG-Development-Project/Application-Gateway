package org.owasp.oag.integration.mockserver;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
class LoginLogoutTest extends WiremockTest {

    @Autowired
    MainConfig mainConfig;

    @Test
    void testLoginGetRedirectUrl() throws Exception {

        // TWO Step test

        // ACT1: Start the login
        var loginResult = webClient.get().uri("/auth/local/login").exchange()
                .expectStatus().isFound().returnResult(String.class);

        var redirectUriString = loginResult.getResponseHeaders().getFirst("Location");
        assert redirectUriString != null;
        URI redirectUri = new URI(redirectUriString);


        AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);
        LoginProvider provider = mainConfig.getLoginProviders().get("local");

        assertTrue(redirectUriString.startsWith((String) provider.getWith().get("authEndpoint")));
        assertEquals(provider.getWith().get("clientId"), oidcRequest.getClientID().toString());

        var loginStateCookie = loginResult.getResponseCookies().getFirst(LoginStateCookie.NAME);
        assert loginStateCookie != null;
        // ACT 2: Call the callback url
        // Arrange
        String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");

        var callbackResult = webClient.get().uri("/auth/local/callback" + authorizationResponse)
                .cookie(loginStateCookie.getName(), loginStateCookie.getValue())
                .exchange()
                .expectStatus().isFound()
                .returnResult(String.class);

        var sessionCookie = callbackResult.getResponseCookies().getFirst(LoginCookie.NAME);
        assertNotNull(sessionCookie);
        var csrfCookie = callbackResult.getResponseCookies().getFirst(CsrfCookie.NAME);
        assertNotNull(csrfCookie);
        // ACT 3: Call the session endpoint
        webClient.get().uri("/auth/session").cookie(sessionCookie.getName(), sessionCookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_AUTHENTICATED);

        // ACT 4: Logout
        var logoutResult = webClient
                .get().uri("/auth/logout")
                .cookie(sessionCookie.getName(), sessionCookie.getValue())
                .cookie(csrfCookie.getName(), csrfCookie.getValue())
                .exchange()
                .expectStatus().isFound()
                .returnResult(String.class);

        //Expect that the cookie is deleted
        var sessionCookie2 = logoutResult.getResponseCookies().getFirst(LoginCookie.NAME);
        assertNotNull(sessionCookie2);
        assertEquals(0, sessionCookie2.getMaxAge().getSeconds());
        assertEquals(sessionCookie.getName(), sessionCookie2.getName());
        assertEquals(sessionCookie.getPath(), sessionCookie2.getPath());
        assertEquals(sessionCookie.getDomain(), sessionCookie2.getDomain());
    }

    @Test
    void testSessionInvalidation() {

        // Arrange: Create session and logout
        LoginResult loginResult = makeLogin();
        loginResult.authenticatedRequest(HttpMethod.GET, "/auth/logout")
                .exchange().expectStatus().isFound();


        // ACT: Use session cookie after logout
        loginResult.authenticatedRequest(HttpMethod.GET, "/auth/session")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_ANONYMOUS);
    }


    @Test
    void testSessionIsAnonymous() {

        webClient.get().uri("/auth/session")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_ANONYMOUS);
    }
}
