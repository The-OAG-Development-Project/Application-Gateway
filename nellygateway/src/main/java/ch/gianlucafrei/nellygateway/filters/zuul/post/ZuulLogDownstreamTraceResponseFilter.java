package ch.gianlucafrei.nellygateway.filters.zuul.post;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.utils.SecureEncoder;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs the downstream trace response to be able to correlate if downstream changed something in the id.
 * This filter should run at the beginning, before any data has been manipulated.
 */
@Component
public class ZuulLogDownstreamTraceResponseFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(ZuulLogDownstreamTraceResponseFilter.class);

    @Autowired
    private NellyConfig config;

    @Autowired
    private TraceContext traceContext;

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        if (traceContext == null || !traceContext.hasCurrentTraceId()) {
            // nothing to do not using trace / correlation Id
            return null;
        }

        RequestContext ctx = RequestContext.getCurrentContext();

        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();

        List<Pair<String, String>> entriesToRemove = new ArrayList<>();
        for (Pair<String, String> entry : zuulResponseHeaders) {
            if (traceContext.getResponseHeader().equals(entry.first())) {
                log.info("Downstream system (route: {}) reports having used this trace-id/correlationId: {}.", ctx.getRouteHost(), SecureEncoder.encodeStringForLog(entry.second(), 128));
                entriesToRemove.add(entry);
            }
        }

        // remove the trace response header, we do not route this through.
        if (entriesToRemove.size() > 0) {
            if (entriesToRemove.size() > 1) {
                log.warn("Received {} duplicate trace headers. Beware a potential attack.", entriesToRemove.size());
            }
            zuulResponseHeaders.removeAll(entriesToRemove);
        }
        return null;
    }

}
