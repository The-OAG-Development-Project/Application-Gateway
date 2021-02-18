package ch.gianlucafrei.nellygateway.logging;

import ch.gianlucafrei.nellygateway.filters.spring.TraceContextFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Implements a no-op Trace / Correlation Id. Effectively disabling correlation-Logging.
 * Configue in the main config with:
 * <code>
 * traceProfile:
 * type: noTrace
 * </code>
 * It does not support any traceImplSpecificSettings and ignores all other settings in the traceProfile.
 */
@Component("noTrace")
public class NoTraceContext implements TraceContext {

    /**
     * Processes the request and does not do any header manipulation up or downstream.
     * What is still being done is to generate a short random string for log correlation.
     * @param exchange
     * @return
     */
    @Override
    public ServerWebExchange processExchange(ServerWebExchange exchange) {

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        exchange.getAttributes().put(TraceContextFilter.CONTEXT_KEY, traceId);

        return exchange;
    }

    public boolean forwardIncomingTrace() {
        return false;
    }

    public boolean acceptAdditionalTraceInfo() {
        return false;
    }

    public boolean sendTraceResponse() {
        return false;
    }
}
