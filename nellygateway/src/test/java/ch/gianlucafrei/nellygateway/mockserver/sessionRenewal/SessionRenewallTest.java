package ch.gianlucafrei.nellygateway.mockserver.sessionRenewal;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.mockserver.MockServerTest;
import ch.gianlucafrei.nellygateway.mockserver.TestFileConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.time.Clock;
import java.time.Duration;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SessionRenewallTest extends MockServerTest {

    @Autowired
    NellyConfig nellyConfig;

    @Autowired
    GlobalClockSource globalClockSource;

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }

    @BeforeEach
    void beforeEach(){
        globalClockSource.setGlobalClock(Clock.systemUTC());
    }

    @Test
    void testSessionRenewal() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie(CsrfCookie.NAME);

        // Time travel
        int sessionDuration = nellyConfig.getSessionBehaviour().getSessionDuration();
        int renewWhenLessThan = nellyConfig.getSessionBehaviour().getRenewWhenLessThan();
        int offset = sessionDuration - renewWhenLessThan + 10; // 10 seconds more to be sure
        globalClockSource.setGlobalClock(
                Clock.offset(globalClockSource.getGlobalClock(), Duration.ofSeconds(offset)));

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/auth/session")
                .cookie(sessionCookie).cookie(csrfCookie))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_AUTHENTICATED))
                .andReturn();

        // Assert
        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie sessionCookie2 = response.getCookie(LoginCookie.NAME);

        assertNotNull(sessionCookie2);
    }

    @Test
    void testSessionRenewalExpiredSession() throws Exception {

        // Arrange
        MvcResult loginCallbackResult = makeLogin();
        Cookie sessionCookie = loginCallbackResult.getResponse().getCookie(LoginCookie.NAME);
        Cookie csrfCookie = loginCallbackResult.getResponse().getCookie(CsrfCookie.NAME);

        // Time travel
        int sessionDuration = nellyConfig.getSessionBehaviour().getSessionDuration();
        int offset = sessionDuration + 10; // 10 seconds more to be sure
        globalClockSource.setGlobalClock(
                Clock.offset(globalClockSource.getGlobalClock(), Duration.ofSeconds(offset)));

        // Act
        MvcResult mvcResult = mockMvc.perform(get("/auth/session")
                .cookie(sessionCookie).cookie(csrfCookie))
                .andExpect(jsonPath("$.state").value(SessionInformation.SESSION_STATE_ANONYMOUS))
                .andReturn();// Any request

        // Assert
        MockHttpServletResponse response = mvcResult.getResponse();
        Cookie sessionCookie2 = response.getCookie(LoginCookie.NAME);

        assertNull(sessionCookie2);
    }
}
