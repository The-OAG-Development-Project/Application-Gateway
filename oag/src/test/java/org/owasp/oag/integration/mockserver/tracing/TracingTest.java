package org.owasp.oag.integration.mockserver.tracing;

import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;

public class TracingTest extends WiremockTest {

    @Test
    void testProxyAddsNotTraceResponse() throws Exception {

        webClient.get().uri("/")
                .exchange()
                .expectHeader().doesNotExist("traceresponse");
    }
}
