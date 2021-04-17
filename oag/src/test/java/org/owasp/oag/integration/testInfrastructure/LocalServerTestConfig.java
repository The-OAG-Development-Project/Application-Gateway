package org.owasp.oag.integration.testInfrastructure;

import org.owasp.oag.config.ConfigLoader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class LocalServerTestConfig {

    /**
     * Returns a custom config file loader that loads the specified test configuration.
     *
     * @return
     */
    @Primary
    @Bean
    ConfigLoader testConfigLoader() {
        return new TestFileConfigLoader("/localServerConfiguration.yaml");
    }
}
