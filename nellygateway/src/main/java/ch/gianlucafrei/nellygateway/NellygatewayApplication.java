package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.AuthenticationFilter;
import ch.gianlucafrei.nellygateway.filters.RewriteFilter;
import ch.gianlucafrei.nellygateway.filters.SimpleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.io.Console;
import java.io.IOException;

@EnableZuulProxy
@SpringBootApplication
public class NellygatewayApplication {

    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    public static NellyConfig config;

    public static void main(String[] args) {

        try {
            config = NellyConfig.load("/Users/gifr/nellygateway/nellygateway/sample-nelly-config.yaml");
        } catch (IOException e) {
            log.error("Could not load nelly configuration", e);
            return;
        }


        SpringApplication.run(NellygatewayApplication.class, args);
        log.info("Nelly started");
    }

    @Bean
    public SimpleFilter simpleFilter() {
        return new SimpleFilter();
    }

    @Bean
    public RewriteFilter rewriteFilterFilter() {
        return new RewriteFilter();
    }

    @Bean
    public AuthenticationFilter authenticationFilterFilter() {
        return new AuthenticationFilter();
    }

}
