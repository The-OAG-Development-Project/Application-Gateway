package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityConfigurationTest extends MockServerTest {

    @Test
    void testProxyBlocksWhenAllowAnonymous() throws Exception {

        // Checks if allowAnonymous: yes works
        this.mockMvc.perform(
                get(TEST_1_ENDPOINT))
                .andExpect(status().is(200));

        // Checks if allowAnonymous: no works
        this.mockMvc.perform(
                get("/secure" + TEST_1_ENDPOINT))
                .andExpect(status().is(401));
    }

    @Test
    void testProxyAddsSecurityHeaders() throws Exception {

        // Makes a request through zuul and check if the security headers are applied
        this.mockMvc.perform(
                get(TEST_1_ENDPOINT))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void testProxyBlocksNoneAllowedMethods() throws Exception {

        this.mockMvc.perform(
                get("/static" + TEST_1_ENDPOINT))
                .andExpect(status().is(200));

        this.mockMvc.perform(MockMvcRequestBuilders
                .delete("/static" + TEST_1_ENDPOINT))
                .andExpect(status().is(405));
    }

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}
