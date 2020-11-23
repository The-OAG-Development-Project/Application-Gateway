package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.crypto.JweEncrypter;
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

    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    @Bean
    public CookieEncryptor cookieEncryptor() throws IOException { return loadCookieEncrypter();}

    public static void main(String[] args) {

        // The global configuration is loaded before Spring starts
        log.debug(String.format("Nell starting... Working directory %s", System.getProperty("user.dir")));
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
}
