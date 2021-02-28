package org.owasp.oag.logging;

import org.owasp.oag.filters.spring.TraceContextFilter;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * This class bridges to the concrete trace context implementation that was configured in the main config yaml (section traceProfile).
 */
@Component
public class TraceContextBridge {

    private static final Logger log = LoggerFactory.getLogger(TraceContextBridge.class);

    @Qualifier("traceContext")
    @Autowired
    private TraceContext implClass;

    public ServerWebExchange processExchange(ServerWebExchange exchange) {

        if (implClass.forwardIncomingTrace()) {
            // make sure we take over the passed in traceparent when it is valid
            String primary = exchange.getRequest().getHeaders().getFirst(implClass.getMainRequestHeader());
            String secondary = null;
            if (implClass.acceptAdditionalTraceInfo()) {
                // make sure we take over the trace state if it is valid
                secondary = exchange.getRequest().getHeaders().getFirst(implClass.getSecondaryRequestHeader());
            }
            try{
                implClass.applyExistingTrace(primary, secondary);
            }
            catch (TraceException e){
                implClass.generateNewTraceId();
            }
        }
        else {
            implClass.generateNewTraceId();
        }

        // Add TraceID to exchange
        exchange.getAttributes().put(TraceContextFilter.TRACE_ID_CONTEXT_KEY, implClass.getTraceString());
        LoggingUtils.logDebug(log, exchange, "Tracing started.");

        // Add trace id to downstream request
        if (implClass.sendTraceDownstream()) {
            LoggingUtils.logDebug(log, exchange, "Added trace id to downstream call.");
            var mutatedRequest = exchange.getRequest().mutate()
                    .header(implClass.getMainRequestHeader(), implClass.getTraceString())
                    .header(implClass.getSecondaryRequestHeader(), implClass.getSecondaryTraceInfoString()).build();

            exchange = exchange.mutate().request(mutatedRequest).build();
        }

        // Add trace id to response
        if (implClass.sendTraceResponse()) {
            LoggingUtils.logDebug(log, exchange, "Adding trace id to upstream response");
            exchange.getResponse().getHeaders().add(implClass.getResponseHeader(), implClass.getTraceResponseString());
        }

        return exchange;
    }
}
