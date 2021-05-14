package org.owasp.oag.integration.mockserver.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfSamesiteStrictTest.TestConfig.class})
class CsrfSamesiteStrictTest extends WiremockTest {

    @Test
    void testCsrfSamesiteStrict() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        // Act
        loginResult.authenticatedRequest(HttpMethod.POST, "/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isOk();
    }

    @Test
    void testCsrfDoubleSubmitCookieNoCookie() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        // Act
        // No csrf cookie (Simulate cross site request)
        loginResult.authenticatedRequestNoCsrf(HttpMethod.POST, "/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testCsrfDoubleSubmitCookieInvalidCsrfToken() throws Exception {

        // Arrange
        var loginResult = makeLogin();
        loginResult.csrfCookie = ResponseCookie.from(CsrfCookie.NAME, "FooBar").build();

        // Act
        loginResult.authenticatedRequest(HttpMethod.POST, "/csrf-samesite-strict/" + TEST_1_ENDPOINT)
                .exchange().expectStatus().isUnauthorized();
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
