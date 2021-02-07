package ch.gianlucafrei.nellygateway.integration.testInfrastructure;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.services.blacklist.SessionBlacklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for integration test. Overwrites the default beans.
 */
@TestConfiguration
@Import(NellygatewayApplication.class)
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
    NellyConfigLoader nellyConfigLoader() {
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
