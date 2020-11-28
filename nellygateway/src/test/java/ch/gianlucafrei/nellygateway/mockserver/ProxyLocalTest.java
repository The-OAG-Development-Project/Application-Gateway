package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProxyLocalTest extends MockServerTest {

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
    void testProxyBody1() throws Exception {

        // Makes a request through zuul and check if the body is correct
        MvcResult mvcResult = this.mockMvc.perform(
                get(TEST_1_ENDPOINT))
                .andExpect(status().is(200))
                .andReturn();

        assertEquals(TEST_1_RESPONSE, mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testProxyBody2() throws Exception {

        // Makes a request through zuul and check if the body is correct
        MvcResult mvcResult = this.mockMvc.perform(
                get(TEST_2_ENDPOINT))
                .andExpect(status().is(200))
                .andReturn();

        assertEquals(TEST_2_RESPONSE, mvcResult.getResponse().getContentAsString());
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


    /**
     * Test if the mock upstream server works correctly
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testMockServer() throws IOException, InterruptedException {

        // Arrange
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(TEST_SERVER_URI + TEST_1_ENDPOINT))
                .build();
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(TEST_SERVER_URI + TEST_2_ENDPOINT))
                .build();

        // Act
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
        assertEquals(TEST_1_RESPONSE, response1.body());
        assertEquals(TEST_2_RESPONSE, response2.body());
    }

    /**
     * Test if the mock upstream server works correctly
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testMockServerNotFound() throws IOException, InterruptedException {

        // Arrange
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEST_SERVER_URI + "/doesNotExist"))
                .build();

        // Act
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(404, response.statusCode());
    }

}
