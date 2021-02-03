package ch.gianlucafrei.nellygateway.reactiveMockServer.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.reactiveMockServer.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.reactiveMockServer.WiremockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

class CsrfLogoutTest extends WiremockTest {

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


    @Test
    void testLogoutCsrfProtectionBlocksRequest() throws Exception {


        // Arrange
        var loginResult = makeLogin();

        // Act
        // No csrf cookie (Simulate cross site request)
        authenticatedRequestNoCsrf(HttpMethod.GET, "/auth/logout", loginResult)
                .exchange().expectStatus().isUnauthorized();

        // Assert, we still have a valid session, no logout was performed
        authenticatedRequestNoCsrf(HttpMethod.GET, "/auth/session", loginResult)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.state").isEqualTo(SessionInformation.SESSION_STATE_AUTHENTICATED);
    }

    @Test
    void testSessionIsAnonymous() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        // Act
        // With csrf samesite cookie, simulate samesite request
        authenticatedRequest(HttpMethod.GET, "/auth/logout", loginResult)
                .exchange()
                .expectStatus().isFound();
    }
}
