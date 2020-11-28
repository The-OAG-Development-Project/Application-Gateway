package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.FileConfigLoader;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.crypto.JweEncrypter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class NellyBeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(NellyBeanConfiguration.class);

    @Bean
    public NellyConfig nellyConfig() {

        try {

            NellyConfigLoader loader = new FileConfigLoader();
            return loader.loadConfiguration();

        } catch (JsonProcessingException e) {
            log.error("Nelly configuration file is invalid: {}", e.getMessage());
            throw new RuntimeException("Nelly configuration file is invalid", e);
        } catch (IOException e) {

            log.error("Could not load nelly configuration {}", e.getMessage());
            throw new RuntimeException("Could not load nelly configuration", e);
        }
    }

    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException {

        if (System.getenv("NELLY-KEY") != null) {
            return JweEncrypter.loadFromEnvironmentVariable("NELLY-KEY");
        } else {
            return JweEncrypter.loadFromFileOrCreateAndStoreNewKey("NELLY.key");
        }
    }
}
