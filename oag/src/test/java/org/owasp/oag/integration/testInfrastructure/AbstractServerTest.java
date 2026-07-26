package org.owasp.oag.integration.testInfrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Base class for tests that call the gateway over HTTPS on a random port. Deliberately carries no
 * annotations, so subclasses keep full control over their Spring test configuration.
 */
public abstract class AbstractServerTest {

    /**
     * The random local port the gateway listens on.
     */
    @LocalServerPort
    protected int serverPort;

    /**
     * Client for calling the gateway under test.
     */
    protected RestTestClient webClient;

    /**
     * Creates the test client once the server port is known.
     */
    @BeforeEach
    public void createTestClient() {
        webClient = TestClientFactory.createClient(serverPort);
    }
}
