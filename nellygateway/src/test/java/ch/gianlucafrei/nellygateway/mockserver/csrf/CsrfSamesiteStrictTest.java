package ch.gianlucafrei.nellygateway.mockserver.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.mockserver.MockServerTest;
import ch.gianlucafrei.nellygateway.mockserver.TestFileConfigLoader;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class CsrfSamesiteStrictTest extends MockServerTest {

    @Autowired
    NellyConfig nellyConfig;

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfSamesiteStrict() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        mockMvc.perform(post("/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie))
                .andExpect(status().is(200));
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookieNoCookie() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // No csrf cookie (Simulate cross site request)
        mockMvc.perform(post("/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie))
                .andExpect(status().is(401));
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfDoubleSubmitCookieInvalidCsrfToken() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");
        csrfCookie.setValue("TESTEST");

        // Act
        // Csrf header but wrong value
        mockMvc.perform(post("/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie))
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
