package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NellygatewayApplication {

    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    @Autowired
    NellyConfig config;

    public static void main(String[] args) {

        SpringApplication.run(NellygatewayApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {

        return args -> {
            log.info("Gateway started with {} routes", config.getRoutes().size());
        };
    }
}
