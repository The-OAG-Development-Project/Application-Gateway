package org.owasp.oag.integration.mockserver.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfSamesiteStrictTest.TestConfig.class})
class CsrfSamesiteStrictTest extends WiremockTest {

    public static final String CSRF_SAMESITE_STRICT = "/csrf-samesite-strict/";

    @Test
    void testCsrfSamesiteStrict() {

        // Arrange
        var loginResult = makeLogin();

        // Act
        loginResult.authenticatedRequest(HttpMethod.POST, CSRF_SAMESITE_STRICT + TEST_1_ENDPOINT)
                .exchange().expectStatus().isOk();
    }

    @Test
    void testCsrfDoubleSubmitCookieNoCookie() {

        // Arrange
        var loginResult = makeLogin();

        // Act
        // No csrf cookie (Simulate cross site request)
        loginResult.authenticatedRequestNoCsrf(HttpMethod.POST, CSRF_SAMESITE_STRICT + TEST_1_ENDPOINT)
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testCsrfDoubleSubmitCookieInvalidCsrfToken() {

        // Arrange
        var loginResult = makeLogin();
        loginResult.csrfCookie = ResponseCookie.from(CsrfCookie.NAME, "FooBar").build();

        // Act
        loginResult.authenticatedRequest(HttpMethod.POST, CSRF_SAMESITE_STRICT + TEST_1_ENDPOINT)
                .exchange().expectStatus().isUnauthorized();
    }

    @TestConfiguration
    public static class TestConfig {

        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
