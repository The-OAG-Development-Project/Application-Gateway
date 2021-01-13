package ch.gianlucafrei.nellygateway.mockserver.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.mockserver.MockServerTest;
import ch.gianlucafrei.nellygateway.mockserver.TestFileConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CsrfLogoutTest extends MockServerTest {

    @Autowired
    NellyConfig nellyConfig;

    @Test
    void testLogoutCsrfProtectionBlocksRequest() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie(CsrfCookie.NAME);

        // Act
        // No csrf cookie (Simulate cross site request)
        mockMvc.perform(get("/auth/logout")
                .cookie(sessionCookie))
                .andExpect(status().is(401));

        // Assert, we still have a valid session, no logout was performed
        mockMvc.perform(get("/auth/session")
                .cookie(sessionCookie))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_AUTHENTICATED));
    }

    @Test
    void testSessionIsAnonymous() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie(CsrfCookie.NAME);

        // Act
        // With csrf samesite cookie, simulate samesite request
        MvcResult logoutResult = mockMvc.perform(
                get("/auth/logout")
                        .cookie(sessionCookie)
                        .cookie(csrfCookie))
                .andExpect(status().is(302))
                .andReturn();

        sessionCookie = logoutResult.getResponse().getCookie(LoginCookie.NAME);

        // Assert, we still have a valid session, no logout was performed
        mockMvc.perform(get("/auth/session")
                .cookie(sessionCookie))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_ANONYMOUS));
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
