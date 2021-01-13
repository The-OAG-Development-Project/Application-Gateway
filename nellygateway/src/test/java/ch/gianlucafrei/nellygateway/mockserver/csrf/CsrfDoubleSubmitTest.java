package ch.gianlucafrei.nellygateway.mockserver.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.mockserver.MockServerTest;
import ch.gianlucafrei.nellygateway.mockserver.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfDoubleSubmitValidation;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class CsrfDoubleSubmitTest extends MockServerTest {

    @Autowired
    NellyConfig nellyConfig;

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookie() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        mockMvc.perform(post("/csrfDoubleSubmit/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie)
                .header(CsrfDoubleSubmitValidation.CSRF_TOKEN_HEADER_NAME, csrfCookie.getValue()))
                .andExpect(status().is(200));
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookieFormParam() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        mockMvc.perform(post("/csrfDoubleSubmit/" + TEST_1_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair(CsrfDoubleSubmitValidation.CSRF_TOKEN_PARAMETER_NAME, csrfCookie.getValue()),
                        new BasicNameValuePair("foo", "bar")
                )))))
                .andExpect(status().is(200));
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookieBlocksWhenNoCsrfToken() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // No csrf cookie value in csrf header or formpost
        mockMvc.perform(post("/csrfDoubleSubmit/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie))
                .andExpect(status().is(401));
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookieBlocksWhenInvalidCsrfToken() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // Csrf header but wrong value
        mockMvc.perform(post("/csrfDoubleSubmit/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie)
                .header("X-csrf", "some other value"))
                .andExpect(status().is(401));
    }

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
