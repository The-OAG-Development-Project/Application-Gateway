package org.owasp.oag.integration.testInfrastructure;

import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * Load a test configuration file from the test resources
 */
public class TestFileConfigLoader extends FileConfigLoader {

    private final String testFileResourceName;

    public TestFileConfigLoader(String testFileResourceName) {
        super(null);
        this.testFileResourceName = testFileResourceName;
    }

    @Override
    public MainConfig loadConfiguration() throws IOException {

        InputStream userConfigInputStream = TestFileConfigLoader.class.getResourceAsStream(testFileResourceName);
        InputStream defaultConfigStream = OWASPApplicationGatewayApplication.class.getResourceAsStream("/default-config.yaml");
        return this.mergeConfiguration(defaultConfigStream, userConfigInputStream);
    }
}
