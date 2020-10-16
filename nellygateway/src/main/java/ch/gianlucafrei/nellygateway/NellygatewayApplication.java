package ch.gianlucafrei.nellygateway;

import ch.gianlucafrei.nellygateway.filters.RewriteFilter;
import ch.gianlucafrei.nellygateway.filters.SimpleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.io.Console;

@EnableZuulProxy
@SpringBootApplication
public class NellygatewayApplication {

    private static Logger log = LoggerFactory.getLogger(NellygatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NellygatewayApplication.class, args);

        System.out.println("Nelly started");
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


}
