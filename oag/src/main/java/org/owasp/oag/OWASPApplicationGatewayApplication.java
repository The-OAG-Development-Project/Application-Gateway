package org.owasp.oag;

import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OWASPApplicationGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(OWASPApplicationGatewayApplication.class);

    @Autowired
    MainConfig config;

    public static void main(String[] args) {

        SpringApplication.run(OWASPApplicationGatewayApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {

        return args -> {
            log.info("OWASP Application Gateway started with {} routes", config.getRoutes().size());
        };
    }
}
