package ch.gianlucafrei.nellygateway.integration.mockserver.csrf;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

class CsrfNoneSubmitTest extends WiremockTest {

    @Autowired
    NellyConfig nellyConfig;

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfNoneTest() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        loginResult.authenticatedRequest(HttpMethod.POST, "/csrf-none/" + TEST_1_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
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
