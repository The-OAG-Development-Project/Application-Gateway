package ch.gianlucafrei.nellygateway.filters.zuul.error;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseFilter extends ZuulFilter {


    protected static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";
    private static final Logger log = LoggerFactory.getLogger(ErrorResponseFilter.class);

    @Autowired
    private NellyConfig config;

    @Override
    public String filterType() {
        return "error";
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        return ctx.getThrowable() != null && !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();

        Throwable throwable = ctx.getThrowable();

        if (throwable instanceof ZuulException) {

            ZuulException exception = (ZuulException) throwable;
            exception.getCause();

            log.warn("Error while routing request: message{} cause={}", exception.getMessage(), exception.errorCause);
        }

        // rest of your code
        return null;
    }
}
