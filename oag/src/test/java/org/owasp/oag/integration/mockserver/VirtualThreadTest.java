package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that gateway requests are handled on virtual threads.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class, VirtualThreadTest.ProbeConfig.class})
public class VirtualThreadTest extends WiremockTest {

    @Test
    void requestsAreHandledOnAVirtualThread() {

        webClient.get().uri(TEST_1_ENDPOINT).exchange().expectStatus().isOk();

        assertTrue(VirtualThreadProbeFilter.wasVirtual(),
                "request was not handled on a virtual thread, it ran on " + VirtualThreadProbeFilter.lastThreadName());
    }

    /**
     * Registers the probe filter, which is not part of OAG's component scan.
     */
    @TestConfiguration
    public static class ProbeConfig {

        /**
         * @return the filter that records whether the request thread is virtual
         */
        @Bean
        public VirtualThreadProbeFilter virtualThreadProbeFilter() {
            return new VirtualThreadProbeFilter();
        }
    }
}
