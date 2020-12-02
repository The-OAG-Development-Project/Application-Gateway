package ch.gianlucafrei.nellygateway.mockserver;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Creates a local server that acts as backend to test the proxy server
 */
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
public class MockServerTest {

    @Autowired
    public MockMvc mockMvc;

    public static HttpServer httpServer;
    public static final int MOCK_SERVER_PORT = 7777;
    public static String TEST_SERVER_URI = "http://localhost:7777";

    public static String TEST_1_ENDPOINT = "/foo";
    public static String TEST_1_RESPONSE = "foo}";

    public static String TEST_2_ENDPOINT = "/bar";
    public static String TEST_2_RESPONSE = "bar";

    @BeforeEach
    public void StartUpMockServer() throws IOException {

        httpServer = HttpServer.create(new InetSocketAddress(MOCK_SERVER_PORT), 0);

        this.setupMockServerRoutes(httpServer);

        httpServer.start();
    }

    public void setupMockServerRoutes(HttpServer httpServer) {

        httpServer.createContext(TEST_1_ENDPOINT, exchange -> {
            byte[] response = TEST_1_RESPONSE.getBytes();

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        httpServer.createContext(TEST_2_ENDPOINT, exchange -> {
            byte[] response = TEST_2_RESPONSE.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        /* Example token response from oidc specification*/
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

        // Simulate token endpoint
        httpServer.createContext("/oidc/token", exchange -> {

            var list = new ArrayList<String>();
            list.add("application/json;charset=UTF-8");
            exchange.getResponseHeaders().put("Content-Type", list);

            byte[] response = tokenResponse.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
    }

    @AfterEach
    public void shutDownMockServer() {

        httpServer.stop(0);
    }
}
