package ch.gianlucafrei.nellygateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class NellygatewayApplication {

    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    public static void main(String[] args) {

        // The global configuration is loaded before Spring starts
        log.debug(String.format("Nell starting... Working directory %s", System.getProperty("user.dir")));
        SpringApplication.run(NellygatewayApplication.class, args);
    }
}
