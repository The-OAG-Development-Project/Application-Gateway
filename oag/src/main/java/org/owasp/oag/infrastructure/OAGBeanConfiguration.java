package org.owasp.oag.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
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
 * Configures all non-trivial beans that can be instanced before the main configuration is loaded.
 * This class serves as the primary Spring Boot configuration for the OAG application,
 * setting up essential services and components.
 */
@Configuration
@ComponentScan(basePackages = {"org.owasp.oag.controllers", "org.owasp.oag.cookies", "org.owasp.oag.filters", "org.owasp.oag.gateway", "org.owasp.oag.hooks", "org.owasp.oag.infrastructure", "org.owasp.oag.logging", "org.owasp.oag.services"})
public class OAGBeanConfiguration {

    /** Logger for this class */
    private static final Logger log = LoggerFactory.getLogger(OAGBeanConfiguration.class);

    /** Global clock source for consistent time across the application */
    @Autowired
    private GlobalClockSource clockSource;

    /**
     * Creates the main configuration bean by loading configuration data from the provided loader.
     * 
     * @param loader The configuration loader to use
     * @return The main application configuration
     * @throws ConfigurationException if the configuration file is invalid or cannot be loaded
     */
    @Bean
    public MainConfig mainConfig(ConfigLoader loader) {

        try {

            MainConfig config = loader.loadConfiguration();
            return config;

        } catch (JsonProcessingException e) {
            throw new ConfigurationException("OAG configuration file is invalid", e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not load OAG configuration", e);
        }
    }

    /**
     * Creates a configuration loader that loads configuration from a file.
     * This bean is lazily initialized to allow for proper configuration path resolution.
     * 
     * @param configPath The path to the configuration file, injected from properties
     * @return A new FileConfigLoader instance
     */
    @Lazy
    @Bean
    ConfigLoader configLoader(@Value("${oag.configPath}") String configPath) {
        return new FileConfigLoader(configPath);
    }

    /**
     * Creates a cookie encryptor for handling encrypted cookie data.
     * Will use an environment variable for the key if available, otherwise creates an in-memory instance.
     * 
     * @return A CookieEncryptor implementation
     * @throws IOException if there's an error loading or creating the encryptor
     */
    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException {

        if (System.getenv("OAG-KEY") != null) {
            return JweEncrypter.loadFromEnvironmentVariable("OAG-KEY");
        } else {
            return JweEncrypter.loadInMemoryInstance();
        }
    }

    /**
     * Creates a session blacklist for tracking invalidated sessions.
     * Uses a local persistent blacklist implementation that stores data in the specified file.
     * The bean's destroy method will properly close the blacklist when the application shuts down.
     * 
     * @param filename The file where blacklist data should be stored, injected from properties
     * @return A SessionBlacklist implementation
     */
    @Bean(destroyMethod = "close")
    public SessionBlacklist sessionBlacklist(@Value("${oag.session-blacklist-file}") String filename) {
        return new LocalPersistentBlacklist(clockSource, filename);
    }
}
