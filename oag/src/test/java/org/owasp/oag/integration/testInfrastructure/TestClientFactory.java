package org.owasp.oag.integration.testInfrastructure;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.web.servlet.client.RestTestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * Creates test clients that talk to the gateway's HTTPS listener. The gateway serves a self-signed
 * certificate generated at build time, so the client must not validate the certificate chain.
 * This trust-all configuration is for tests only and must never be used in production code.
 * <p>
 * Hostname verification stays enabled: the generated certificate carries {@code CN=localhost} and
 * subject alternative names for {@code localhost} and {@code 127.0.0.1} (see {@code build.gradle}).
 */
public final class TestClientFactory {

    /**
     * Time a test waits for a gateway response before failing.
     */
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    private TestClientFactory() {
    }

    /**
     * Creates a client bound to the gateway running on the given local port.
     *
     * @param port the local HTTPS port of the gateway under test
     * @return a client that trusts the gateway's self-signed certificate
     */
    public static RestTestClient createClient(int port) {
        return createClient(port, DEFAULT_RESPONSE_TIMEOUT);
    }

    /**
     * Creates a client bound to the gateway running on the given local port, waiting at most the
     * given time for a response.
     *
     * @param port            the local HTTPS port of the gateway under test
     * @param responseTimeout how long to wait for a gateway response
     * @return a client that trusts the gateway's self-signed certificate
     */
    public static RestTestClient createClient(int port, Duration responseTimeout) {

        var httpClient = HttpClient.newBuilder()
                .sslContext(trustAllSslContext())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(responseTimeout);

        return RestTestClient.bindToServer(requestFactory)
                .baseUrl("https://localhost:" + port)
                .build();
    }

    private static SSLContext trustAllSslContext() {

        var trustAll = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        try {
            var sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, new TrustManager[]{trustAll}, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a trust-all SSL context for tests", e);
        }
    }
}
