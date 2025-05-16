package org.owasp.oag.integration.mockserver.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.owasp.oag.services.csrf.CsrfDoubleSubmitCookieValidation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * Integration tests for CSRF protection using the double submit cookie pattern.
 * These tests verify that the CSRF protection correctly validates tokens submitted
 * through headers or form parameters, and properly blocks requests without valid tokens.
 * <p>
 * The double submit cookie pattern requires the same CSRF token to be present in both
 * a cookie and either a request header or form parameter.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfDoubleSubmitTest.TestConfig.class})
class CsrfDoubleSubmitTest extends WiremockTest {

    /** Base path for CSRF double submit test endpoints */
    public static final String CSRF_DOUBLE_SUBMIT = "/csrf-double-submit/";

    /**
     * Tests that a POST request with a valid CSRF token in the header is successful.
     * The token is submitted via a request header and must match the token in the CSRF cookie.
     */
    @Test
    void testCsrfDoubleSubmitCookie() {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .header(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, loginResult.csrfCookie.getValue())
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Tests that a POST request with a valid CSRF token in a form parameter is successful.
     * The token is submitted as a form parameter and must match the token in the CSRF cookie.
     */
    @Test
    void testCsrfDoubleSubmitCookieFormParam() {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        var formData = BodyInserters.fromFormData(
                CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_PARAMETER_NAME, loginResult.csrfCookie.getValue());

        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Tests that a POST request with an incorrect CSRF token value in a form parameter is blocked.
     * The request should be rejected with 401 Unauthorized when the submitted token
     * doesn't match the token in the CSRF cookie.
     */
    @Test
    void testCsrfDoubleSubmitCookieFormParamWrongValue() {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(
                        CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_PARAMETER_NAME, "Foobar"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Tests that a POST request without any CSRF token is blocked.
     * The request should be rejected with 401 Unauthorized when no token is provided
     * in either a header or form parameter.
     */
    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenNoCsrfToken() {

        // Arrange
        LoginResult loginResult = makeLogin();

        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .exchange().expectStatus().isUnauthorized();
    }

    /**
     * Tests that a POST request with an invalid CSRF token is blocked.
     * The request should be rejected with 401 Unauthorized when the submitted token
     * doesn't match the expected token.
     */
    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenInvalidCsrfToken() {

        // Arrange
        LoginResult loginResult = makeLogin();
        loginResult.csrfCookie = ResponseCookie.from(CsrfCookie.NAME, "someOtherValue").build();

        // Act
        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .header(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, loginResult.csrfCookie.getValue())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Test configuration class that provides a custom ConfigLoader for test purposes.
     * This allows the tests to use a specific configuration file.
     */
    @TestConfiguration
    public static class TestConfig {

        /**
         * Creates a test-specific ConfigLoader that loads configuration from a test resource file.
         * 
         * @return A ConfigLoader instance pointing to the test configuration file
         */
        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
