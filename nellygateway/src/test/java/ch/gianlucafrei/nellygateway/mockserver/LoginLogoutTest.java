package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.LoginProvider;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginLogoutTest extends MockServerTest {

    @Autowired
    NellyConfig nellyConfig;

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }

    @Override
    public void setupMockServerRoutes(HttpServer httpServer) {

        super.setupMockServerRoutes(httpServer);

        /* Example token response from oidc specification*/
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

        // Simulate token endpoint
        httpServer.createContext("/oidc/token", exchange -> {

            var list = new ArrayList<String>();
            list.add("application/json;charset=UTF-8");
            exchange.getResponseHeaders().put("Content-Type", list);

            byte[] response = tokenResponse.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
    }


    @Test
    void testLoginGetRedirectUrl() throws Exception {

        // TWO Step test

        // ACT1: Start the login
        MvcResult loginResult = this.mockMvc.perform(
                get("/auth/local/login"))
                .andExpect(status().is(302))
                .andReturn();

        // Assert
        String redirectUriString = loginResult.getResponse().getHeader("Location");
        URI redirectUri = new URI(redirectUriString);

        AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);
        LoginProvider provider = nellyConfig.getLoginProviders().get("local");

        assertTrue(redirectUriString.startsWith((String) provider.getWith().get("authEndpoint")));
        assertEquals(provider.getWith().get("clientId"), oidcRequest.getClientID().toString());

        Cookie loginStateCookie = loginResult.getResponse().getCookie(LoginStateCookie.NAME);

        // ACT 2: Call the callback url
        // Arrange
        String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");

        MvcResult callbackResult = mockMvc.perform(
                get("/auth/local/callback" + authorizationResponse).cookie(loginStateCookie))
                .andExpect(status().is(302))
                .andReturn();

        Cookie sessionCookie = callbackResult.getResponse().getCookie(LoginCookie.NAME);
        assertNotNull(sessionCookie);

        // ACT 3: Call the session endpoint
        mockMvc.perform(
                get("/auth/session").cookie(sessionCookie))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_AUTHENTICATED));


        // ACT 4: Logout
        MvcResult logoutResult = mockMvc.perform(
                get("/auth/logout").cookie(sessionCookie))
                .andExpect(status().is(302))
                .andReturn();

        //Expect that the cookie is deleted
        Cookie sessionCookie2 = logoutResult.getResponse().getCookie(LoginCookie.NAME);
        assertEquals(0, sessionCookie2.getMaxAge());
        assertEquals(sessionCookie.getName(), sessionCookie2.getName());
        assertEquals(sessionCookie.getPath(), sessionCookie2.getPath());
        assertEquals(sessionCookie.getDomain(), sessionCookie2.getDomain());
    }

    @Test
    void testSessionIsAnonymous() throws Exception {

        mockMvc.perform(
                get("/auth/session")) // no session cookie
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_ANONYMOUS));
    }
}
