package ch.gianlucafrei.nellygateway.reactiveMockServer.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.reactiveMockServer.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.reactiveMockServer.WiremockTest;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfDoubleSubmitValidation;
import org.junit.jupiter.api.Test;
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
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
