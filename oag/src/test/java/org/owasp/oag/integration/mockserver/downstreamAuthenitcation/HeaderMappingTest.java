package org.owasp.oag.integration.mockserver.downstreamAuthenitcation;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HeaderMappingTest extends WiremockTest{

    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {

        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/headerUserMappingConfig.yaml");
        }
    }

    @Test
    public void testDownstreamAuthentication(){

        // Arrange
        LoginResult loginResult = makeLogin();

        // Act
        authenticatedRequest(HttpMethod.GET, TEST_1_ENDPOINT, loginResult)
                .exchange()
                .expectStatus().isOk();

        // Assert
        verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
                .withHeader("X-USER-ID", matching(loginResult.id))
                .withHeader("X-USER-PROVIDER", matching("local")));
    }
}
