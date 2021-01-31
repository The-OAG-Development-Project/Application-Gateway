package ch.gianlucafrei.nellygateway.filters.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Order(1)
@Component
public class SimpleLogFilter extends GlobalFilterBase {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    public void filter(ServerWebExchange exchange) {

        var request = exchange.getRequest();

        log.info("Request to {} {}",
                request.getMethod(),
                request.getURI());
    }

    @Override
    protected void onSuccess(ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        log.info("Response status code {} for {} {}",
                response.getRawStatusCode(),
                request.getMethodValue(),
                request.getURI());
    }


    @Override
    protected void onError(Throwable t, ServerWebExchange exchange) {

        var request = exchange.getRequest();

        log.info("Error {} during request processing for {} {}",
                t.getMessage(),
                request.getMethod(),
                request.getURI());

        throw new RuntimeException("Error during request processing", t);
    }
}
