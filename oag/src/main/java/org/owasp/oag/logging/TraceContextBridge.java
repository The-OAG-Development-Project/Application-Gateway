package org.owasp.oag.logging;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.TraceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * This class bridges to the concrete trace context implementation that was configured in the main config yaml (section traceProfile).
 */
@Component
@Primary
public class TraceContextBridge {

    private final Supplier<TraceContext> traceContexts;

    /**
     * Creates the bridge for the trace type configured in {@code traceProfile.type}.
     *
     * @param config   The main configuration
     * @param registry The trace context registry
     * @throws org.owasp.oag.exception.ConfigurationException If the configured trace type is unknown
     */
    @Autowired
    public TraceContextBridge(MainConfig config, TraceContextRegistry registry) {
        this(registry.get(config.getTraceProfile().getType())::create);
    }

    /**
     * Creates the bridge with the given source of trace contexts.
     *
     * @param traceContexts Supplies a fresh trace context for each request
     */
    public TraceContextBridge(Supplier<TraceContext> traceContexts) {
        this.traceContexts = traceContexts;
    }

    /**
     * Decides the tracing headers for one request: adopts a valid incoming trace when the trace profile
     * allows it, otherwise generates a new trace id.
     *
     * @param incomingPrimary   value of the main trace request header, or null if absent
     * @param incomingSecondary value of the secondary trace request header, or null if absent
     * @return the trace id plus the headers to add downstream and to the response
     */
    public TraceHeaders createTraceHeaders(String incomingPrimary, String incomingSecondary) {

        final TraceContext requestContext = traceContexts.get();

        if (requestContext.forwardIncomingTrace()) {
            if (incomingPrimary == null || incomingPrimary.isEmpty()) {
                // No inbound trace to adopt - the ordinary case for a first-hop request. It must not
                // go through TraceException, or every such request logs. Note the header accessor
                // yields "" rather than null for a header sent with an empty value.
                requestContext.generateNewTraceId();
            } else {
                try {
                    requestContext.applyExistingTrace(incomingPrimary,
                            requestContext.acceptAdditionalTraceInfo() ? incomingSecondary : null);
                } catch (TraceException e) {
                    requestContext.generateNewTraceId();
                }
            }
        } else {
            requestContext.generateNewTraceId();
        }

        var downstreamHeaders = new LinkedHashMap<String, String>();
        if (requestContext.sendTraceDownstream()) {
            downstreamHeaders.put(requestContext.getMainRequestHeader(), requestContext.getTraceString());
            if (requestContext.acceptAdditionalTraceInfo()) {
                // A freshly generated trace carries no secondary info, and a header with a null value
                // is meaningless - omit it rather than forwarding an empty one.
                var secondaryTraceInfo = requestContext.getSecondaryTraceInfoString();
                if (secondaryTraceInfo != null) {
                    downstreamHeaders.put(requestContext.getSecondaryRequestHeader(), secondaryTraceInfo);
                }
            }
        }

        var responseHeaders = new LinkedHashMap<String, String>();
        if (requestContext.sendTraceResponse()) {
            responseHeaders.put(requestContext.getResponseHeader(), requestContext.getTraceResponseString());
        }

        return new TraceHeaders(requestContext.getTraceString(), downstreamHeaders, responseHeaders);
    }

    /**
     * @return the name of the main trace request header for the configured trace profile
     */
    public String getMainRequestHeader() {
        return traceContexts.get().getMainRequestHeader();
    }

    /**
     * @return the name of the secondary trace request header for the configured trace profile
     */
    public String getSecondaryRequestHeader() {
        return traceContexts.get().getSecondaryRequestHeader();
    }
}
