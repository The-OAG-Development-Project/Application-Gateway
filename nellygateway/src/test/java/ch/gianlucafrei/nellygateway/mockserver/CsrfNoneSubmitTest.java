package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
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
class CsrfNoneSubmitTest extends MockServerTest {

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


    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfNoneTest() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie("csrf");

        // Act
        // No csrf cookie value in csrf header or formpost
        mockMvc.perform(post("/csrf-none/" + TEST_1_ENDPOINT)
                .cookie(sessionCookie)
                .cookie(csrfCookie))
                .andExpect(status().is(200));
    }
}
