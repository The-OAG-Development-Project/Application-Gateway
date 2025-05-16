package org.owasp.oag.integration.mockserver.tracing;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Integration tests for verifying that tracing is correctly disabled when using the 'noTracing' configuration.
 * These tests ensure that when tracing is disabled, no correlation IDs or other tracing headers
 * are included in responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, NoTracingTest.PathTestConfig.class})
public class NoTracingTest extends WiremockTest {

    /**
     * Test configuration class that provides a custom ConfigLoader for testing the no-tracing configuration.
     * This allows the tests to use a specific configuration file that has tracing disabled.
     */
    @TestConfiguration
    public static class PathTestConfig {

        /**
         * Creates a test-specific ConfigLoader that loads configuration from a test resource file
         * with tracing disabled.
         *
         * @return A ConfigLoader instance pointing to the no-tracing configuration file
         */
        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/noTracingConfiguration.yaml");
        }
    }

   /**
    * Verifies that when tracing is disabled, no correlation ID headers are included in the response.
    * This test sends a request to a test endpoint and checks that the X-Correlation-Id header
    * is not present in the response, confirming that tracing is correctly disabled.
    */
   @Test
   void testNoTracing(){

       webClient.get().uri(TEST_1_ENDPOINT)
               .exchange()
               .expectHeader().doesNotExist("X-Correlation-Id")
               .expectStatus().isOk();
   }
}
