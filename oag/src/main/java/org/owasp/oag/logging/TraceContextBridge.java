package org.owasp.oag.logging;

import org.owasp.oag.exception.TraceException;
import org.owasp.oag.filters.spring.TraceContextFilter;
import org.owasp.oag.infrastructure.factories.TraceContextFactory;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * This class bridges to the concrete trace context implementation that was configured in the main config yaml (section traceProfile).
 */
@Component
@Primary
public class TraceContextBridge {

    private static final Logger log = LoggerFactory.getLogger(TraceContextBridge.class);

    @Autowired
    private TraceContextFactory traceContextFactory;

    /**
     * Processes a ServerWebExchange by creating a trace context, applying or generating a trace ID,
     * and adding it to the exchange, downstream requests, and responses as needed.
     *
     * @param exchange The ServerWebExchange to process
     * @return The processed ServerWebExchange with trace information added
     */
    public ServerWebExchange processExchange(ServerWebExchange exchange) {

        final TraceContext requestContext = traceContextFactory.createContextForRequest();

        if (requestContext.forwardIncomingTrace()) {
            // make sure we take over the passed in traceparent when it is valid
            String primary = exchange.getRequest().getHeaders().getFirst(requestContext.getMainRequestHeader());
            String secondary = null;
            if (requestContext.acceptAdditionalTraceInfo()) {
                // make sure we take over the trace state if it is valid
                secondary = exchange.getRequest().getHeaders().getFirst(requestContext.getSecondaryRequestHeader());
            }
            try{
                requestContext.applyExistingTrace(primary, secondary);
            }
            catch (TraceException e){
                requestContext.generateNewTraceId();
            }
        }
        else {
            requestContext.generateNewTraceId();
        }

        // Add TraceID to exchange
        exchange.getAttributes().put(TraceContextFilter.TRACE_ID_CONTEXT_KEY, requestContext.getTraceString());
        LoggingUtils.logDebug(log, exchange, "Tracing started.");

        // Add trace id to downstream request
        if (requestContext.sendTraceDownstream()) {
            LoggingUtils.logDebug(log, exchange, "Added trace id to downstream call.");
            var mutatedRequest = exchange.getRequest().mutate();
            mutatedRequest.header(requestContext.getMainRequestHeader(), requestContext.getTraceString());
            if(requestContext.acceptAdditionalTraceInfo()) {
                mutatedRequest.header(requestContext.getSecondaryRequestHeader(), requestContext.getSecondaryTraceInfoString());
            }

            exchange = exchange.mutate().request(mutatedRequest.build()).build();
        }

        // Add trace id to response
        if (requestContext.sendTraceResponse()) {
            LoggingUtils.logDebug(log, exchange, "Adding trace id to upstream response");
            exchange.getResponse().getHeaders().add(requestContext.getResponseHeader(), requestContext.getTraceResponseString());
        }

        return exchange;
    }
}
