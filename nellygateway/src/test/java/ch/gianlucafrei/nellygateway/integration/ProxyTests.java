package ch.gianlucafrei.nellygateway.integration;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("This test uses the real demo backend")
@SpringBootTest()
@AutoConfigureMockMvc
class ProxyTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NellyConfig nellyConfig;

    @Autowired
    private ApplicationContext context;

    @Test
    void testProxyAddsSecurityHeaders() throws Exception {

        // Makes a request through zuul and check if the security headers are applied
        this.mockMvc.perform(
                get("/"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void testProxyBlocksWhenAllowAnonymous() throws Exception {

        // Checks if allowAnonymous: yes works
        this.mockMvc.perform(
                get("/"))
                .andExpect(status().is2xxSuccessful());

        // Checks if allowAnonymous: no works
        this.mockMvc.perform(
                get("/secure/"))
                .andExpect(status().is(401));
    }
}
