package ch.gianlucafrei.nellygateway.filters.zuul.route;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adds Trace (Correlation Id) information for the down stream system depending on configuration.
 * See
 */
@Component
public class ZuulAddTraceHeaderDownstreamFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(ZuulAddTraceHeaderDownstreamFilter.class);

    @Autowired
    private NellyConfig config;

    @Autowired
    private TraceContext traceContext;

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 90;
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

        ctx.addZuulRequestHeader(traceContext.getMainRequestHeader(), traceContext.getTraceString());

        if (traceContext.forwardIncomingTrace() && traceContext.acceptAdditionalTraceInfo()) {
            ctx.addZuulRequestHeader(traceContext.getSecondaryRequestHeader(), traceContext.getSecondaryTraceInfoString());
        }

        return null;
    }
}
