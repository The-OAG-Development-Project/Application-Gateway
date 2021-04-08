package org.owasp.oag.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.services.blacklist.LocalPersistentBlacklist;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.services.crypto.JweEncrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

/**
 * Configures all non-trivial beans that can be instanced before the main configuration is loaded
 */
@Configuration
@ComponentScan(basePackages={"org.owasp.oag"})
public class OAGBeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OAGBeanConfiguration.class);

    @Autowired
    private GlobalClockSource clockSource;

    @Autowired
    @Bean
    public MainConfig mainConfig(ConfigLoader loader){

        try {

            MainConfig config = loader.loadConfiguration();
            return config;

        } catch (JsonProcessingException e) {
            log.error("OAG configuration file is invalid: {}", e.getMessage());
            throw new RuntimeException("OAG configuration file is invalid", e);
        } catch (IOException e) {

            log.error("Could not load OAG configuration {}", e.getMessage());
            throw new RuntimeException("Could not load OAG configuration", e);
        }
    }

    @Lazy
    @Bean
    ConfigLoader configLoader(@Value("${oag.configPath}") String configPath) {

        return new FileConfigLoader(configPath);
    }

    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException {

        if (System.getenv("OAG-KEY") != null) {
            return JweEncrypter.loadFromEnvironmentVariable("OAG-KEY");
        } else {
            return JweEncrypter.loadFromFileOrCreateAndStoreNewKey("OAG.key");
        }
    }

    @Bean(destroyMethod = "close")
    public SessionBlacklist sessionBlacklist(@Value("${oag.session-blacklist-file}") String filename) {
        return new LocalPersistentBlacklist(clockSource, filename);
    }
}
