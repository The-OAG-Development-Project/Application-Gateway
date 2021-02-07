package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.FileConfigLoader;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.services.blacklist.LocalPersistentBlacklist;
import ch.gianlucafrei.nellygateway.services.blacklist.SessionBlacklist;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.crypto.JweEncrypter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class NellyBeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(NellyBeanConfiguration.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GlobalClockSource clockSource;

    @Bean
    public NellyConfig nellyConfig() {

        try {

            NellyConfigLoader loader = nellyConfigLoader();
            NellyConfig config = loader.loadConfiguration();

            var configErrors = config.getErrors(context);
            if (!configErrors.isEmpty()) {
                String message = "Configuration file contains errors: " + configErrors.toString();
                log.error(message);
                throw new RuntimeException(message);
            }

            return config;

        } catch (JsonProcessingException e) {
            log.error("Nelly configuration file is invalid: {}", e.getMessage());
            throw new RuntimeException("Nelly configuration file is invalid", e);
        } catch (IOException e) {

            log.error("Could not load nelly configuration {}", e.getMessage());
            throw new RuntimeException("Could not load nelly configuration", e);
        }
    }

    @Bean
    NellyConfigLoader nellyConfigLoader() {
        return new FileConfigLoader();
    }

    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException {

        if (System.getenv("NELLY-KEY") != null) {
            return JweEncrypter.loadFromEnvironmentVariable("NELLY-KEY");
        } else {
            return JweEncrypter.loadFromFileOrCreateAndStoreNewKey("NELLY.key");
        }
    }

    @Bean(destroyMethod = "close")
    public SessionBlacklist sessionBlacklist(@Value("${oag.session-blacklist-file}") String filename) {
        return new LocalPersistentBlacklist(clockSource, filename);
    }
}
