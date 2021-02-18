package ch.gianlucafrei.nellygateway.integration.mockserver.tracing;

import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.IntegrationTestConfig;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
        NellyConfigLoader nellyConfigLoader() {
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
