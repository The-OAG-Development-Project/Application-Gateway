package org.owasp.oag.integration.mockserver;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.integration.testInfrastructure.IntegrationTestConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.integration.testInfrastructure.WiremockTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class PathRewriteTest extends WiremockTest {

    @Test
    public void urlRewriteTestDefault() {

        /**
         rewriteTest1:
         type: webapplication
         path: /rewrite1/**
         url: http://localhost:7777/rewritten/
         allowAnonymous: yes
         */

        var msg = "This is the Message";
        stubFor(get(urlEqualTo("/rewritten/message.txt"))
                .willReturn(aResponse().withBody(msg)));

        webClient
                .get().uri("/rewrite1/message.txt")
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(msg);
    }

    @Test
    public void urlRewriteTestCustomConfig() {

        /**

         rewriteTest2:
         type: webapplication
         path: /rewrite2/**
         url: http://localhost:7777/rewritten/
         allowAnonymous: yes
         rewrite:
         regex: "/rewrite2/abc.txt"
         replacement: "/abc/def.ghi"

         */

        var msg = "This is the Message";
        stubFor(get(urlEqualTo("/abc/def.ghi"))
                .willReturn(aResponse().withBody(msg)));

        webClient
                .get().uri("/rewrite2/abc.txt")
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(msg);

        webClient
                .get().uri("/rewrite2/doesnotexist")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void urlRewriteTestCustomConfig2() {

        /**

         rewriteTest3:
         type: webapplication
         path: /rewrite3/**
         url: http://localhost:7777/rewritten/
         allowAnonymous: yes
         rewrite:
         regex: "^(.)*$"
         replacement: "/index.html"

         */

        var msg = "This is the Message";
        stubFor(get(urlEqualTo("/index.html"))
                .willReturn(aResponse().withBody(msg)));

        webClient
                .get().uri("/rewrite3/anything.abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody().equals(msg);
    }

    @Configuration
    @Import(IntegrationTestConfig.class)
    public static class PathTestConfig {

        @Primary
        @Bean
        ConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/rewriteTestConfig.yaml");
        }
    }
}
