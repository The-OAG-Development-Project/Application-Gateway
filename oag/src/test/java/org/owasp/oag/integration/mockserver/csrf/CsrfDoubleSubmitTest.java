package org.owasp.oag.integration.mockserver.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.owasp.oag.services.csrf.CsrfDoubleSubmitValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.BodyInserters;

class CsrfDoubleSubmitTest extends WiremockTest {

    @Test
    void testCsrfDoubleSubmitCookie() throws Exception {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        authenticatedRequest(HttpMethod.POST, "/csrfDoubleSubmit/" + TEST_1_ENDPOINT, loginResult)
                .header(CsrfDoubleSubmitValidation.CSRF_TOKEN_HEADER_NAME, loginResult.csrfCookie.getValue())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testCsrfDoubleSubmitCookieFormParam() throws Exception {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        var formData = BodyInserters.fromFormData(
                CsrfDoubleSubmitValidation.CSRF_TOKEN_PARAMETER_NAME, loginResult.csrfCookie.getValue());

        authenticatedRequest(HttpMethod.POST, "/csrfDoubleSubmit/" + TEST_1_ENDPOINT, loginResult)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testCsrfDoubleSubmitCookieFormParamWrongValue() throws Exception {

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        authenticatedRequest(HttpMethod.POST, "/csrfDoubleSubmit/" + TEST_1_ENDPOINT, loginResult)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(
                        CsrfDoubleSubmitValidation.CSRF_TOKEN_PARAMETER_NAME, "Foobar"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenNoCsrfToken() throws Exception {

        // Arrange
        LoginResult loginResult = makeLogin();

        authenticatedRequest(HttpMethod.POST, "/csrfDoubleSubmit/" + TEST_1_ENDPOINT, loginResult)
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testCsrfDoubleSubmitCookieBlocksWhenInvalidCsrfToken() throws Exception {

        // Arrange
        LoginResult loginResult = makeLogin();
        loginResult.csrfCookie = ResponseCookie.from(CsrfCookie.NAME, "someOtherValue").build();

        // Act
        authenticatedRequest(HttpMethod.POST, "/csrfDoubleSubmit/" + TEST_1_ENDPOINT, loginResult)
                .header(CsrfDoubleSubmitValidation.CSRF_TOKEN_HEADER_NAME, loginResult.csrfCookie.getValue())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Configuration
    @Import(OWASPApplicationGatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        ConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
