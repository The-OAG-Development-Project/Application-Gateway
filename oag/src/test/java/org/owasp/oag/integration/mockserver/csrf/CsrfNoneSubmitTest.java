package org.owasp.oag.integration.mockserver.csrf;

import io.github.artsok.RepeatedIfExceptionsTest;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
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
                "logging.level.ch.gianlucafrei=TRACE"},
        classes = {IntegrationTestConfig.class, CsrfNoneSubmitTest.TestConfig.class})
class CsrfNoneSubmitTest extends WiremockTest {

    @Autowired
    MainConfig mainConfig;

    @RepeatedIfExceptionsTest(repeats = 5)
    void testCsrfNoneTest() throws Exception {

        // Arrange
        var loginResult = makeLogin();

        loginResult.authenticatedRequest(HttpMethod.POST, "/csrf-none/" + TEST_1_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
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
