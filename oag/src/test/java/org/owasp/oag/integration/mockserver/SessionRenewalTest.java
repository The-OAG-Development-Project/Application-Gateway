package org.owasp.oag.integration.mockserver;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
class SessionRenewalTest extends WiremockTest {

    @Autowired
    GlobalClockSource globalClockSource;

    @Autowired
    CookieConverter cookieConverter;

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        globalClockSource.setGlobalClock(Clock.systemUTC());
    }

    @Test
    void testSessionRenewal() {

        // Arrange
        var loginResult = makeLogin();

        // Time travel
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();
        int renewWhenLessThan = config.getSessionBehaviour().getRenewWhenLessThan();
        int offset = sessionDuration - renewWhenLessThan + 10; // 10 seconds more to be sure
        globalClockSource.setGlobalClock(
                Clock.offset(globalClockSource.getGlobalClock(), Duration.ofSeconds(offset)));

        // Act
        var result = webClient.get().uri("/auth/session")
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists(LoginCookie.NAME)
                .expectBody(String.class).returnResult();

        // Assert
        assertTrue(result.getResponseBody().contains(SessionInformation.SESSION_STATE_AUTHENTICATED), result.getResponseBody());
        var oldCookie = decrypt(loginResult.sessionCookie);
        var renewedCookie = decrypt(result.getResponseCookies().getFirst(LoginCookie.NAME));
        assertNotEquals(oldCookie.getId(), renewedCookie.getId());
        assertNotNull(oldCookie.getCsrfToken());
        assertEquals(oldCookie.getCsrfToken(), renewedCookie.getCsrfToken());
    }

    private LoginCookie decrypt(ResponseCookie cookie) {
        return cookieConverter.convertLoginCookie(new Cookie(cookie.getName(), cookie.getValue()));
    }

    @Test
    void testSessionRenewalExpiredSession() {

        // Arrange
        var loginResult = makeLogin();

        // Time travel
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();
        int offset = sessionDuration + 10; // 10 seconds more to be sure
        globalClockSource.setGlobalClock(
                Clock.offset(globalClockSource.getGlobalClock(), Duration.ofSeconds(offset)));

        // Act
        webClient.get().uri("/auth/session")
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .exchange()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_ANONYMOUS);
    }
}
