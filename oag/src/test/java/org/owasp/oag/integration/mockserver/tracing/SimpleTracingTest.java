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

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleTracingTest extends WiremockTest {


    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {

        @Primary
        @Bean
        ConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/simpleTracingConfiguration.yaml");
        }
    }


    /**
     * Tests that a new trace id is generated if none exists yet and forwarded downstream
     */
   @Test
   void testSimpleTracing(){

       var result = webClient.get().uri(TEST_1_ENDPOINT)
               .exchange()
               .expectHeader().exists("X-Correlation-Id")
               .returnResult(String.class);

       var traceString = result.getResponseHeaders().getFirst("X-Correlation-Id");

       assertNotNull(traceString);

       verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
               .withHeader("TRACE-ID", equalTo(traceString)));
   }

    /**
     * Tests that ab existing trace id is corrently picked up and forwarded to teh downstream system
     */
   @Test
   void testSimpleTracingExistingId(){

       var traceId = UUID.randomUUID().toString();

       var result = webClient.get().uri(TEST_1_ENDPOINT)
               .header("TRACE-ID", traceId)
               .exchange()
               .expectHeader().exists("X-Correlation-Id")
               .returnResult(String.class);

       var traceString = result.getResponseHeaders().getFirst("X-Correlation-Id");

       assertNotNull(traceString);

       verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
               .withHeader("TRACE-ID", equalTo(traceString)));
   }
}
