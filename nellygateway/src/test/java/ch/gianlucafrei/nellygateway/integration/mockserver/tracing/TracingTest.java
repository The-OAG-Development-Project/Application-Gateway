package ch.gianlucafrei.nellygateway.integration.mockserver.tracing;

import ch.gianlucafrei.nellygateway.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;

public class TracingTest extends WiremockTest {

    @Test
    void testProxyAddsNotTraceResponse() throws Exception {

        webClient.get().uri("/")
                .exchange()
                .expectHeader().doesNotExist("traceresponse");
    }
}
