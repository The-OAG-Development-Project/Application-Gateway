package ch.gianlucafrei.nellygateway.mockserver;

import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Creates a local server that acts as backend to test the proxy server
 */
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
public class MockServerTest {

    @Autowired
    public MockMvc mockMvc;

    private static final Logger log = LoggerFactory.getLogger(MockServerTest.class);

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
        }).getFilters().add(logging());
        ;

        httpServer.createContext(TEST_2_ENDPOINT, exchange -> {
            byte[] response = TEST_2_RESPONSE.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }).getFilters().add(logging());
        ;

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
        }).getFilters().add(logging());
    }

    private static Filter logging() {
        return new Filter() {

            @Override
            public void doFilter(HttpExchange http, Chain chain) throws IOException {
                try {
                    chain.doFilter(http);
                } finally {

                    log.info(String.format("%s %s %s %s",
                            http.getRequestMethod(),
                            http.getRequestURI().getPath(),
                            http.getRemoteAddress(),
                            http.getRequestHeaders().getFirst("User-Agent")));
                }
            }

            @Override
            public String description() {
                return "logging";
            }
        };
    }

    @AfterEach
    public void shutDownMockServer() {

        httpServer.stop(0);
    }
    
    public MvcResult makeLogin() throws Exception {

        MvcResult loginResult = this.mockMvc.perform(
                get("/auth/local/login"))
                .andExpect(status().is(302))
                .andReturn();

        // Assert
        String redirectUriString = loginResult.getResponse().getHeader("Location");
        URI redirectUri = new URI(redirectUriString);

        AuthenticationRequest oidcRequest = AuthenticationRequest.parse(redirectUri);

        Cookie loginStateCookie = loginResult.getResponse().getCookie(LoginStateCookie.NAME);

        // ACT 2: Call the callback url
        // Arrange
        String authorizationResponse = String.format("?state=%s&code=%s", oidcRequest.getState().getValue(), "authCode");

        MvcResult callbackResult = mockMvc.perform(
                get("/auth/local/callback" + authorizationResponse).cookie(loginStateCookie))
                .andExpect(status().is(302))
                .andReturn();

        return callbackResult;
    }
}
