package org.owasp.oag.integration.mockserver.tracing;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, NoTracingTest.PathTestConfig.class})
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
