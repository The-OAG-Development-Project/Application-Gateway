package org.owasp.oag.logging.w3ctrace;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.spring.TraceContextFilter;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.logging.TraceException;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * Implements the W3C Trace Context specification.
 * It supports only singe tracestate headers as incomming requests
 * See https://w3c.github.io/trace-context/
 * <p>
 * Configue in the main config with:
 * <code>
 * traceProfile:
 * type: w3cTrace
 * </code>
 * It does not support any traceImplSpecificSettings in the configuration and ignores the maxLengthIncomingTrace setting as this is defined by the specification.
 */
@Component("w3cTrace")
public class W3cTraceContext implements TraceContext {
    private static final String W3C_MAIN_RESPONSE_HEADER_NAME = "traceresponse";
    private static final String W3C_MAIN_HEADER_NAME = "traceparent";
    private static final String W3C_SUB_HEADER_NAME = "tracestate";

    private static final Logger log = LoggerFactory.getLogger(W3cTraceContext.class);

    @Autowired
    private MainConfig config;

    @Override
    public ServerWebExchange processExchange(ServerWebExchange exchange) {

        // Load trace id from request or create new
        W3cTraceContextState ctx;
        if (forwardIncomingTrace()) {
            // make sure we take over the passed in traceparent when it is valid
            String primary = exchange.getRequest().getHeaders().getFirst(getMainRequestHeader());
            String secondary = null;
            if (acceptAdditionalTraceInfo()) {
                // make sure we take over the trace state if it is valid
                secondary = exchange.getRequest().getHeaders().getFirst(getSecondaryRequestHeader());
            }
            try{
                ctx = new W3cTraceContextState(primary, secondary, config.getTraceProfile().getMaxLengthAdditionalTraceInfo());
            }
            catch (TraceException e){
                ctx = new W3cTraceContextState();
            }
        }
        else {
            ctx = new W3cTraceContextState();
        }

        // Add TraceID to exchange
        exchange.getAttributes().put(TraceContextFilter.CONTEXT_KEY, ctx.getTraceString());

        // Add trace if to downstream request
        if (forwardIncomingTrace()) {

            var mutatedRequest = exchange.getRequest().mutate()
                    .header(getMainRequestHeader(), ctx.getTraceString())
                    .header(getSecondaryRequestHeader(), ctx.getSecondaryTraceInfoString()).build();

            exchange = exchange.mutate().request(mutatedRequest).build();
        }

        // Add trace id to response
        if (sendTraceResponse()) {
            LoggingUtils.logDebug(log, exchange, "Adding trace id to response");
            exchange.getResponse().getHeaders().add(getResponseHeader(), ctx.getTraceString());
        }

        return exchange;
    }

    /**
     * @return the header that should be used when a client sends a correlationId
     */
    public String getMainRequestHeader() {
        return W3C_MAIN_HEADER_NAME;
    }

    /**
     * @return the header that should be used when a client sends secondary information (i.e. vendor specific) for a trace
     */
    public String getSecondaryRequestHeader() {
        return W3C_SUB_HEADER_NAME;
    }

    /**
     * @return the header that should be used when we respond the correlation id upstream
     */
    public String getResponseHeader() {
        return W3C_MAIN_RESPONSE_HEADER_NAME;
    }

    /**
     * @return true when a passed in traceparent should be re-used and passed on downstream
     */
    public boolean forwardIncomingTrace() {
        return config.getTraceProfile().getForwardIncomingTrace();
    }

    /**
     * @return true when also the tracestate passed in should be forwarded downstream
     */
    public boolean acceptAdditionalTraceInfo() {
        return config.getTraceProfile().getAcceptAdditionalTraceInfo();
    }

    /**
     * @return true when we send the caller the correlation id (traceparent) we used
     */
    public boolean sendTraceResponse() {
        return config.getTraceProfile().getSendTraceResponse();
    }
}
