package ch.gianlucafrei.nellygateway.reactiveMockServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureWireMock(port = 7777)
public class WiremockTest {

    public static String TEST_SERVER_URI = "http://localhost:7777";
    public static String TEST_1_ENDPOINT = "/foo";
    public static String TEST_1_RESPONSE = "foo}";
    public static String TEST_2_ENDPOINT = "/bar";
    public static String TEST_2_RESPONSE = "bar";
    public static String TEST_NOTFOUND = "/notfound";

    @Autowired
    protected WebTestClient webClient;

    @BeforeEach
    public void beforeEach() throws Exception {

        //Stubs
        stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/delay/3"))
                .willReturn(aResponse()
                        .withBody("no fallback")
                        .withFixedDelay(3000)));

        stubFor(any(urlEqualTo(TEST_1_ENDPOINT))
                .willReturn(aResponse().withBody(TEST_1_RESPONSE)));

        stubFor(any(urlEqualTo(TEST_2_ENDPOINT))
                .willReturn(aResponse().withBody(TEST_2_RESPONSE)));

        stubFor(any(urlEqualTo(TEST_NOTFOUND))
                .willReturn(aResponse().withBody("Not Found").withStatus(404)));

        // Stub authorization server
        String tokenResponse = "{" +
                "   \"access_token\": \"SlAV32hkKG\"," +
                "   \"token_type\": \"Bearer\"," +
                "   \"refresh_token\": \"8xLOxBtZp8\"," +
                "   \"expires_in\": 3600," +
                "   \"id_token\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzc" +
                "     yI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5" +
                "     NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZ" +
                "     fV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5Nz" +
                "     AKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6q" +
                "     Jp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJ" +
                "     NqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7Tpd" +
                "     QyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoS" +
                "     K5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4" +
                "     XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg\"" +
                "  }";
        stubFor(post(urlEqualTo("/oidc/token"))
                .willReturn(aResponse()
                        .withBody(tokenResponse)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")));
    }
}
