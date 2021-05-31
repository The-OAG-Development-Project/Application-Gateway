package org.owasp.oag.integration.mockserver.tracing;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
public class TracingTest extends WiremockTest {

    @Test
    void testProxyAddsNotTraceResponse() throws Exception {

        webClient.get().uri("/")
                .exchange()
                .expectHeader().doesNotExist("traceresponse");
    }
}
