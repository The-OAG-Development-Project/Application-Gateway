package org.owasp.oag.integration.testInfrastructure;

import org.owasp.oag.GlobalClockSource;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for integration test. Overwrites the default beans.
 */
@TestConfiguration
@Import(OWASPApplicationGatewayApplication.class)
public class IntegrationTestConfig {

    @Autowired
    GlobalClockSource clockSource;

    /**
     * Returns a custom config file loader that loads the specified test configuration.
     *
     * @return
     */
    @Primary
    @Bean
    ConfigLoader nellyConfigLoader() {
        return new TestFileConfigLoader("/localServerConfiguration.yaml");
    }

    /**
     * Uses a in-memory blacklist for sessions
     *
     * @return
     */
    @Primary
    @Bean(destroyMethod = "close")
    public SessionBlacklist sessionBlacklist() {
        return new LocalInMemoryBlacklist(clockSource);
    }
}
