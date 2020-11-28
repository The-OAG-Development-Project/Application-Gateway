package ch.gianlucafrei.nellygateway.mockserver;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

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


    @BeforeAll
    public static void StartUpMockServer() throws IOException {

        httpServer = HttpServer.create(new InetSocketAddress(MOCK_SERVER_PORT), 0);

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

        httpServer.start();
    }

    @AfterAll
    public static void shutDownMockServer() {

        httpServer.stop(0);
    }
}
