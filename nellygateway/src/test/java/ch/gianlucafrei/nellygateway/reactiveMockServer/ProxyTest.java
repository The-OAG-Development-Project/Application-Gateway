package ch.gianlucafrei.nellygateway.reactiveMockServer;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

public class ProxyTest extends WiremockTest {

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }

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
