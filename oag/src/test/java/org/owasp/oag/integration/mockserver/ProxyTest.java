package org.owasp.oag.integration.mockserver;

import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.junit.jupiter.api.Test;

public class ProxyTest extends WiremockTest {

    @Test
    public void testHttpProxy() {

        webClient
                .get().uri(TEST_1_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(TEST_1_RESPONSE);

        webClient
                .get().uri(TEST_2_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(TEST_2_RESPONSE);

        webClient
                .get().uri(TEST_NOTFOUND)
                .exchange()
                .expectStatus().isEqualTo(404);
    }
}
