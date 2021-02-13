package ch.gianlucafrei.nellygateway.integration;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testLoadRoutes() {

        // Arrange (Load configuration)
        var nellyRoutes = nellyConfig.getRoutes().entrySet();
        ZuulProperties zuulProperties = (ZuulProperties) context.getBean("zuul.CONFIGURATION_PROPERTIES");
        Map<String, ZuulProperties.ZuulRoute> zuulRoutes = zuulProperties.getRoutes();

        // ACT
        nellyRoutes.forEach(nellyRouteEntry -> {

            var id = nellyRouteEntry.getKey();
            var nellyRoute = nellyRouteEntry.getValue();

            var zuulRoute = zuulRoutes.get(id);

            assertEquals(nellyRoute.getPath(), zuulRoute.getPath());
            assertEquals(nellyRoute.getUrl(), zuulRoute.getUrl());
        });
    }

    @Test
    void testProxyAddsSecurityHeaders() throws Exception {

        // Makes a request through zuul and check if the security headers are applied
        this.mockMvc.perform(
                get("/"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void testProxyAddsNotTraceResponse() throws Exception {

        // Makes a request through zuul and check if the security headers are fine
        this.mockMvc.perform(
                get("/"))
                .andExpect(header().doesNotExist("traceresponse"));
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
