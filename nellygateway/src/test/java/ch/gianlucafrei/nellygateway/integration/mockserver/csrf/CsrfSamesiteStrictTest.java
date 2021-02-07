package ch.gianlucafrei.nellygateway.integration.mockserver.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;

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
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
