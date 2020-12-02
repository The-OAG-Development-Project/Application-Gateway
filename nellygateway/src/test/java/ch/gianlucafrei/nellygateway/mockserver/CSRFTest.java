package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.net.URI;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CSRFTest extends MockServerTest {

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

    private MvcResult makeLogin() throws Exception {

        MvcResult loginResult = this.mockMvc.perform(
                get("/auth/local/login"))
                .andExpect(status().is(302))
                .andReturn();

        // Assert
        String redirectUriString = loginResult.getResponse().getHeader("Location");
        URI redirectUri = new URI(redirectUriString);

        AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);

        Cookie loginStateCookie = loginResult.getResponse().getCookie(LoginStateCookie.NAME);

        // ACT 2: Call the callback url
        // Arrange
        String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");

        MvcResult callbackResult = mockMvc.perform(
                get("/auth/local/callback" + authorizationResponse).cookie(loginStateCookie))
                .andExpect(status().is(302))
                .andReturn();

        return callbackResult;
    }

    @Test
    void testCsrfDoubleSubmitCookie() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // Baseline cookie in header
        mockMvc.perform(post("/csrfDoubleSubmit/")
                .cookie(sessionCookie)
                .cookie(csrfCookie)
                .header("csrf", csrfCookie.getValue()))
                .andExpect(status().is(200));

        // Baseline cookie in form post
        mockMvc.perform(post("/csrfDoubleSubmit/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("csrf", csrfCookie.getValue()),
                        new BasicNameValuePair("foo", "bar")
                )))))
                .andExpect(status().is(200));
    }

    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenNoCsrfToken() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // No csrf cookie value in csrf header or formpost
        mockMvc.perform(post("/csrfDoubleSubmit/")
                .cookie(sessionCookie)
                .cookie(csrfCookie))
                .andExpect(status().is(401));
    }

    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenInvalidCsrfToken() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // Csrf header but wrong value
        mockMvc.perform(post("/csrfDoubleSubmit/")
                .cookie(sessionCookie)
                .cookie(csrfCookie)
                .header("csrf", "some other value"))
                .andExpect(status().is(401));
    }
}
