package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.FileConfigLoader;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.crypto.JweEncrypter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class NellyBeanConfiguration {

    @Bean
    public NellyConfig nellyConfig() {

        try {

            NellyConfigLoader loader = new FileConfigLoader();
            return loader.loadConfiguration();

        } catch (IOException e) {
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
