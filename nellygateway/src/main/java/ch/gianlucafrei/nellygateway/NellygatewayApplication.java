package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.config.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.AuthenticationFilter;
import ch.gianlucafrei.nellygateway.filters.ResponseHeaderFilter;
import ch.gianlucafrei.nellygateway.filters.SimpleFilter;
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

    public static void main(String[] args) {

        log.debug(String.format("Nell starting... Working directory %s", System.getProperty("user.dir")));

        try {
            config = NellyConfig.load(
                    "sample-nelly-config.yaml",
                    "sample-nelly-config-secret.yaml");
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

    /*@Bean
    public RewriteFilter rewriteFilterFilter() {
        return new RewriteFilter();
    }*/

    @Bean
    public AuthenticationFilter authenticationFilterFilter() {
        return new AuthenticationFilter();
    }

    @Bean
    public ResponseHeaderFilter responseHeaderFilter() {return new ResponseHeaderFilter();}
}
