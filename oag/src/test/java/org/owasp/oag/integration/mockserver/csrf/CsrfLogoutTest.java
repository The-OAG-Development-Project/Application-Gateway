package org.owasp.oag.integration.mockserver.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfLogoutTest.TestConfig.class})
class CsrfLogoutTest extends WiremockTest {

    @Autowired
    MainConfig mainConfig;

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

    @Configuration
    @Import(OWASPApplicationGatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
