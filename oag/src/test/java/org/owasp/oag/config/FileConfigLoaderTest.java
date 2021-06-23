package org.owasp.oag.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileConfigLoaderTest {

    /**
     * Test if the FileConfigLoader correctly request a configuration file via HTTPS if
     * the configuration path is a https url
     * @throws Exception
     */
    @Test
    void loadConfigFileViaHttps() throws Exception {

        // Arrange
        String configFile = "hostUri: http://testest\n";

        WireMockServer wireMockServer = new WireMockServer(wireMockConfig());
        wireMockServer.start();
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/oagConfiguration"))
                .willReturn(aResponse().withBody(configFile).withStatus(200)));

        String configUrl = wireMockServer.baseUrl() + "/oagConfiguration";
        FileConfigLoader loader = new FileConfigLoader(configUrl);
        loader.enableUnsafeHttps();

        // Act
        MainConfig mainConfig = loader.loadConfiguration();

        // Assert
        assertNotNull(mainConfig);
        assertEquals("http://testest", mainConfig.getHostUri());

        // Clean up
        wireMockServer.stop();
    }

    /**
     * Tests if the FileConfigLoader correctly loads a configuration file from disk
     * @throws Exception
     */
    @Test
    void loadConfigFromFile() throws Exception{

        // Arrange
        FileConfigLoader loader = new FileConfigLoader("sample-config.yaml");

        // Act
        MainConfig mainConfig = loader.loadConfiguration();

        // Assert
        assertNotNull(mainConfig);
        assertEquals("http://localhost:8080", mainConfig.getHostUri());

    }
}