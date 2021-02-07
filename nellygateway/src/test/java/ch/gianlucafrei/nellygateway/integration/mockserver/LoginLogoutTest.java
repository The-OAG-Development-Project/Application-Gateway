package ch.gianlucafrei.nellygateway.integration.mockserver;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProvider;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginLogoutTest extends WiremockTest {

    @Autowired
    NellyConfig nellyConfig;

    @Test
    void testLoginGetRedirectUrl() throws Exception {

        // TWO Step test

        // ACT1: Start the login
        var loginResult = webClient.get().uri("/auth/local/login").exchange()
                .expectStatus().isFound().returnResult(String.class);

        var redirectUriString = loginResult.getResponseHeaders().getFirst("Location");
        URI redirectUri = new URI(redirectUriString);


        AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);
        LoginProvider provider = nellyConfig.getLoginProviders().get("local");

        assertTrue(redirectUriString.startsWith((String) provider.getWith().get("authEndpoint")));
        assertEquals(provider.getWith().get("clientId"), oidcRequest.getClientID().toString());

        var loginStateCookie = loginResult.getResponseCookies().getFirst(LoginStateCookie.NAME);

        // ACT 2: Call the callback url
        // Arrange
        String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");
        var callbackResult = webClient.get().uri("/auth/local/callback" + authorizationResponse)
                .cookie(loginStateCookie.getName(), loginStateCookie.getValue())
                .exchange()
                .expectStatus().isFound()
                .returnResult(String.class);

        var sessionCookie = callbackResult.getResponseCookies().getFirst(LoginCookie.NAME);
        var csrfCookie = callbackResult.getResponseCookies().getFirst(CsrfCookie.NAME);

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
    void testSessionIsAnonymous() throws Exception {

        webClient.get().uri("/auth/session")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_ANONYMOUS);
    }
}
