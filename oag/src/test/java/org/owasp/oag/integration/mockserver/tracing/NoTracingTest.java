package org.owasp.oag.integration.mockserver.tracing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NoTracingTest extends WiremockTest {


    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {

        @Primary
        @Bean
        ConfigLoader configLoader() {
            return new TestFileConfigLoader("/noTracingConfiguration.yaml");
        }
    }

   @Test
   void testNoTracing(){

       webClient.get().uri(TEST_1_ENDPOINT)
               .exchange()
               .expectHeader().doesNotExist("X-Correlation-Id")
               .expectStatus().isOk();
   }
}
