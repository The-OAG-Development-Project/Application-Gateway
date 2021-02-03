package ch.gianlucafrei.nellygateway.reactiveMockServer;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.FileConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;

import java.io.IOException;
import java.io.InputStream;

public class TestFileConfigLoader extends FileConfigLoader {

    private String testFileResourceName;

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
