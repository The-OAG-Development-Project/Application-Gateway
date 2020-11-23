package ch.gianlucafrei.nellygateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProxyTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testProxyAddsSecurityHeaders() throws Exception {

        // Makes a request through zuul and check if the security headers are applied
        this.mockMvc.perform(
                get("/"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void testProxyBlocksWhenAllowAnonymousNo() throws Exception {

        // Checks if allowAnonymous: yes works
        this.mockMvc.perform(
                get("/"))
                .andExpect(status().is2xxSuccessful());

        // Checks if allowAnonymous: no works
        this.mockMvc.perform(
                get("/secure/"))
                .andExpect(status().is4xxClientError());
    }

}
