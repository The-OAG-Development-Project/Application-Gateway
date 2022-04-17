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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfDoubleSubmitTest.TestConfig.class})
class CsrfDoubleSubmitTest extends WiremockTest {

    public static final String CSRF_DOUBLE_SUBMIT = "/csrf-double-submit/";

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

    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenNoCsrfToken() {

        // Arrange
        LoginResult loginResult = makeLogin();

        authenticatedRequest(HttpMethod.POST, CSRF_DOUBLE_SUBMIT + TEST_1_ENDPOINT, loginResult)
                .exchange().expectStatus().isUnauthorized();
    }

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

    @TestConfiguration
    public static class TestConfig {

        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
