package org.owasp.oag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the OWASP Application Gateway.
 * This class initializes and runs the Spring Boot application.
 */
@SpringBootApplication
public class OWASPApplicationGatewayApplication {

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {

        SpringApplication.run(OWASPApplicationGatewayApplication.class, args);
    }
}
