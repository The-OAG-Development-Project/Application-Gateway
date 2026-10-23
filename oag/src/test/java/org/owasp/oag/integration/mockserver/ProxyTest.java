package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.LocalServerTestConfig;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "logging.level.org.owasp.oag=TRACE"},
        classes = {IntegrationTestConfig.class, LocalServerTestConfig.class})
public class ProxyTest extends WiremockTest {

    @Test
    public void testHttpProxy() {

        webClient
                .get().uri(TEST_1_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(TEST_1_RESPONSE);

        webClient
                .get().uri(TEST_2_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(TEST_2_RESPONSE);

        webClient
                .get().uri(TEST_NOTFOUND)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    /**
     * A form-urlencoded body of a method that the servlet specification does not source parameters
     * from must still reach the downstream system unchanged. The gateway must not let the servlet
     * container consume such a body while populating the parameter map.
     */
    @Test
    public void testHttpProxyForwardsFormBodyOfPutRequest() {

        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("field", "value");

        webClient
                .method(HttpMethod.PUT).uri(TEST_1_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .exchange()
                .expectStatus().isOk();

        verify(putRequestedFor(urlEqualTo(TEST_1_ENDPOINT))
                .withRequestBody(equalTo("field=value")));
    }
}
