package org.owasp.oag.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.services.blacklist.LocalPersistentBlacklist;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.services.crypto.JweEncrypter;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configures all non-trivial beans that can be instanced before the main configuration is loaded
 */
@Configuration
public class OAGBeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OAGBeanConfiguration.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GlobalClockSource clockSource;

    @Bean
    public MainConfig mainConfig() {

        try {

            ConfigLoader loader = configLoader();
            MainConfig config = loader.loadConfiguration();

            var configErrors = config.getErrors(context);
            if (!configErrors.isEmpty()) {
                String message = "Configuration file contains errors: " + configErrors.toString();
                log.error(message);
                throw new RuntimeException(message);
            }

            return config;

        } catch (JsonProcessingException e) {
            log.error("OAG configuration file is invalid: {}", e.getMessage());
            throw new RuntimeException("OAG configuration file is invalid", e);
        } catch (IOException e) {

            log.error("Could not load OAG configuration {}", e.getMessage());
            throw new RuntimeException("Could not load OAG configuration", e);
        }
    }

    @Bean
    ConfigLoader configLoader() {
        return new FileConfigLoader();
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

    public static CsrfProtectionValidation loadCsrfValidationImplementation(String csrfProtectionMethod, ApplicationContext context) {

        String beanname = "csrf-" + csrfProtectionMethod + "-validation";
        CsrfProtectionValidation validationImplementation = context.getBean(beanname, CsrfProtectionValidation.class);

        if (validationImplementation == null) {
            throw new RuntimeException("csrf validation implementation not found: " + beanname);
        }

        return validationImplementation;
    }
}
