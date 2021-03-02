package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Duration;

class SessionRenewallTest extends WiremockTest {

    @Autowired
    GlobalClockSource globalClockSource;

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        globalClockSource.setGlobalClock(Clock.systemUTC());
    }

    @Test
    void testSessionRenewal() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        // Time travel
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();
        int renewWhenLessThan = config.getSessionBehaviour().getRenewWhenLessThan();
        int offset = sessionDuration - renewWhenLessThan + 10; // 10 seconds more to be sure
        globalClockSource.setGlobalClock(
                Clock.offset(globalClockSource.getGlobalClock(), Duration.ofSeconds(offset)));

        // Act
        webClient.get().uri("/auth/session")
                .cookie(loginResult.sessionCookie.getName(), loginResult.sessionCookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists(LoginCookie.NAME)
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_AUTHENTICATED);
    }

    @Test
    void testSessionRenewalExpiredSession() throws Exception {

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
