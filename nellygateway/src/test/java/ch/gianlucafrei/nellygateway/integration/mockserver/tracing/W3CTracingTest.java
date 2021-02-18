package ch.gianlucafrei.nellygateway.integration.mockserver.tracing;

import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.IntegrationTestConfig;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.TestFileConfigLoader;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class W3CTracingTest extends WiremockTest {

    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/W3CTracingConfiguration.yaml");
        }
    }

    /**
     * Test that a new traceparent is set if no traceparent is present and forwarded to the downstream service
     */
   @Test
   void testW3CTracing(){

       var result = webClient.get().uri(TEST_1_ENDPOINT)
               .exchange()
               .expectHeader().exists("traceresponse")
               .returnResult(String.class);

       var traceString = result.getResponseHeaders().getFirst("traceresponse");

       String sechzehnNull = "0000000000000000";
       assertTrue(traceString.startsWith("00-"));
       assertTrue(traceString.endsWith("-01"));
       assertFalse(traceString.contains(sechzehnNull));
       assertEquals(traceString.length(), 2 + 1 + 32 + 1 + 16 + 1 + 2);

       verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
               .withHeader("traceparent",equalTo(traceString)));
   }

    /**
     * Tests that a valid trace id from the traceparent header is forwarded the the downstream service
     */
   @Test
   void testSimpleTracingExistingId(){

       var traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
       var traceState  = "rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";


       var result = webClient.get().uri(TEST_1_ENDPOINT)
               .header("traceparent", traceParent)
               .header("tracestate", traceState)
               .exchange()
               .expectHeader().exists("traceresponse")
               .returnResult(String.class);

       var traceString = result.getResponseHeaders().getFirst("traceresponse");

       assertEquals(traceParent, traceString);
       verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
               .withHeader("traceparent",equalTo(traceParent))
               .withHeader("traceState", equalTo(traceState)));

   }

    /**
     * Tests that an invalid existing traceparent is rejected and a new traceparent is generated
     */
    @Test
    void testSimpleTracingInvalidExistingId(){

        var traceParent = "00-00000000000000000000000000000000-b7ad6b7169203331-01";
        var traceState  = "rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";


        var result = webClient.get().uri(TEST_1_ENDPOINT)
                .header("traceparent", traceParent)
                .header("tracestate", traceState)
                .exchange()
                .expectHeader().exists("traceresponse")
                .returnResult(String.class);

        var traceString = result.getResponseHeaders().getFirst("traceresponse");

        assertNotEquals(traceParent, traceString);
        verify(getRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
                .withHeader("traceparent",equalTo(traceString)));

    }
}
