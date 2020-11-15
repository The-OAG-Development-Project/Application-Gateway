package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.zuul.AccessControlFilter;
import ch.gianlucafrei.nellygateway.filters.zuul.AuthenticationHeadersFilter;
import ch.gianlucafrei.nellygateway.filters.zuul.ResponseHeaderFilter;
import ch.gianlucafrei.nellygateway.services.crypto.JweEncrypter;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@EnableZuulProxy
@SpringBootApplication
public class NellygatewayApplication {

    public static NellyConfig config;
    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException { return loadCookieEncrypter();}

    public static void main(String[] args) {

        // The global configuration is loaded before Spring starts
        log.debug(String.format("Nell starting... Working directory %s", System.getProperty("user.dir")));

        try{
            loadConfiguration();
        }
        catch (Exception e)
        {
            log.error("Startup failed", e);
            return;
        }

        SpringApplication.run(NellygatewayApplication.class, args);
    }

    private static CookieEncryptor loadCookieEncrypter() throws IOException {


        if(System.getenv("NELLY-KEY") != null) {
            return JweEncrypter.loadFromEnvironmentVariable("NELLY-KEY");
        }
        else{
            return JweEncrypter.loadFromFileOrCreateAndStoreNewKey("NELLY.key");
        }

    }

    public static void loadConfiguration() throws IOException {

        String configPath;

        if(System.getenv("NELLY_CONFIG_PATH") != null)
            configPath = System.getenv("NELLY_CONFIG_PATH");
        else
            configPath = "sample-nelly-config.yaml"; // Default path if we have no config


        NellygatewayApplication.config = NellyConfig.load(configPath,null);

        log.debug("Configuration loaded from {}", configPath);
    }
}
