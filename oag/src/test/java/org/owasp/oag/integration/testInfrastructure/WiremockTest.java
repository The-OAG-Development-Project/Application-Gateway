package org.owasp.oag.integration.testInfrastructure;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for all tests that use the gateway functionality.
 * Adds a mock server on port 7777 with some default methods for testing.
 */
@AutoConfigureWireMock(port = 0)
public abstract class WiremockTest {

    public static String TEST_1_ENDPOINT = "/foo";
    public static String TEST_1_RESPONSE = "foo}";
    public static String TEST_2_ENDPOINT = "/bar";
    public static String TEST_2_RESPONSE = "bar";
    public static String TEST_NOTFOUND = "/notfound";

    @Autowired
    protected MainConfig config;

    @Autowired
    protected WebTestClient webClient;

    @BeforeEach
    public void beforeEach() throws Exception {

        //Stubs
        stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/delay/3"))
                .willReturn(aResponse()
                        .withBody("no fallback")
                        .withFixedDelay(3000)));

        stubFor(any(urlEqualTo(TEST_1_ENDPOINT))
                .willReturn(aResponse().withBody(TEST_1_RESPONSE)));

        stubFor(any(urlEqualTo(TEST_2_ENDPOINT))
                .willReturn(aResponse().withBody(TEST_2_RESPONSE)));

        stubFor(any(urlEqualTo(TEST_NOTFOUND))
                .willReturn(aResponse().withBody("Not Found").withStatus(404)));

        // Stub authorization server
        String tokenResponse = "{" +
                "   \"access_token\": \"SlAV32hkKG\"," +
                "   \"token_type\": \"Bearer\"," +
                "   \"refresh_token\": \"8xLOxBtZp8\"," +
                "   \"expires_in\": 3600," +
                "   \"id_token\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzc" +
                "     yI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5" +
                "     NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZ" +
                "     fV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5Nz" +
                "     AKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6q" +
                "     Jp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJ" +
                "     NqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7Tpd" +
                "     QyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoS" +
                "     K5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4" +
                "     XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg\"" +
                "  }";
        stubFor(post(urlEqualTo("/oidc/token"))
                .willReturn(aResponse()
                        .withBody(tokenResponse)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")));
    }

    protected LoginResult makeLogin() {

        try {
            // ACT1: Start the login
            var loginResult = webClient.get().uri("/auth/local/login").exchange()
                    .expectStatus().isFound().returnResult(String.class);

            var redirectUriString = loginResult.getResponseHeaders().getFirst("Location");
            assertNotNull(redirectUriString);
            URI redirectUri = new URI(redirectUriString);

            AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);
            LoginProvider provider = config.getLoginProviders().get("local");

            assertTrue(redirectUriString.startsWith((String) provider.getWith().get("authEndpoint")));
            assertEquals(provider.getWith().get("clientId"), oidcRequest.getClientID().toString());

            var loginStateCookie = loginResult.getResponseCookies().getFirst(LoginStateCookie.NAME);
            assertNotNull(loginStateCookie);
            // ACT 2: Call the callback url
            // Arrange
            String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");
            var callbackResult = webClient.get().uri("/auth/local/callback" + authorizationResponse)
                    .cookie(loginStateCookie.getName(), loginStateCookie.getValue())
                    .exchange()
                    .expectStatus().isFound()
                    .returnResult(String.class);

            var result = new LoginResult(callbackResult);
            result.id = "248289761001"; // id from jwt token

            return result;
        } catch (Exception e) {
            throw new ApplicationException("Login Failed", e);
        }
    }

    protected WebTestClient.RequestBodySpec authenticatedRequest(HttpMethod method, String uri, LoginResult loginResult) {
        return webClient.method(method).uri(uri)
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .cookie(loginResult.csrfCookie.getName(), loginResult.csrfCookie.getValue());
    }

    protected WebTestClient.RequestBodySpec authenticatedRequestNoCsrf(HttpMethod method, String uri, LoginResult loginResult) {
        return webClient.method(method).uri(uri)
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue());
    }

    public class LoginResult {

        public ResponseCookie sessionCookie;
        public ResponseCookie csrfCookie;
        public String id;

        public LoginResult(FluxExchangeResult<String> callbackResult) {

            this.sessionCookie = callbackResult.getResponseCookies().getFirst(LoginCookie.NAME);
            this.csrfCookie = callbackResult.getResponseCookies().getFirst(CsrfCookie.NAME);
        }

        public WebTestClient.RequestBodySpec authenticatedRequest(HttpMethod method, String uri) {
            return WiremockTest.this.authenticatedRequest(method, uri, this);
        }

        public WebTestClient.RequestBodySpec authenticatedRequestNoCsrf(HttpMethod method, String uri) {
            return WiremockTest.this.authenticatedRequestNoCsrf(method, uri, this);
        }

    }
}
