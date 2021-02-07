package ch.gianlucafrei.nellygateway.integration.testInfrastructure;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.FileConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * Load a test configuration file from the test resources
 */
public class TestFileConfigLoader extends FileConfigLoader {

    private final String testFileResourceName;

    public TestFileConfigLoader(String testFileResourceName) {
        this.testFileResourceName = testFileResourceName;
    }

    @Override
    public NellyConfig loadConfiguration() throws IOException {

        InputStream userConfigInputStream = TestFileConfigLoader.class.getResourceAsStream(testFileResourceName);
        InputStream defaultConfigStream = NellygatewayApplication.class.getResourceAsStream("/default-config.yaml");
        return this.load(defaultConfigStream, userConfigInputStream);
    }
}
