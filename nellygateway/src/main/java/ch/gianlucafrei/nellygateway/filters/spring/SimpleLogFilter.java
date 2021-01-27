package ch.gianlucafrei.nellygateway.filters.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class SimpleLogFilter extends GlobalFilterBase {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    public void filter() {


        log.info("Request to {} {}",
                request.getMethod(),
                request.getURI());
    }

    @Override
    protected void onSuccess() {

        log.info("Response status code {} for {} {}",
                response.getRawStatusCode(),
                request.getMethodValue(),
                request.getURI());
    }


    @Override
    protected void onError(Throwable t) {

        log.info("Error {} during request processing for {} {}",
                t.getMessage(),
                request.getMethod(),
                request.getURI());

        throw new RuntimeException("Error during request processing", t);
    }
}
