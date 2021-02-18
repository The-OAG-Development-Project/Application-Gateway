package ch.gianlucafrei.nellygateway.logging.simpleTrace;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.spring.TraceContextFilter;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.utils.SecureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logDebug;

/**
 * Implements simple trace / correlation logging.
 * For downstream calls uses an X-Correlation-Id header as default (or whatever is defined in the custom extension property headerName).
 * The Trace id is a simple GUID/UUID when generated self and accepts whatever value is provided in case of forwarding (cr/lf not allowed).
 * Configue in the main config with:
 * <code>
 * traceProfile:
 * type: simpleTrace
 * traceImplSpecificSettings:
 * headerName: "Other-Header-Name-Than-X-Correlation-Id"
 * </code>
 * It does not support additional trace info and ignores these configured values.
 */
@Component("simpleTrace")
public class SimpleTraceContext implements TraceContext {
    /**
     * Name of the traceImplSpecificSettings setting that contains the header name that should be used instead of  the default.
     */
    private static final String HEADER_NAME = "headerName";
    /**
     * Name of the http header used per default to transport the trace/correlation id.
     */
    private static final String DEFAULT_HEADER = "X-Correlation-Id";

    private static final Logger log = LoggerFactory.getLogger(SimpleTraceContext.class);

    private volatile String appliedHeaderName = null;

    @Autowired
    private NellyConfig config;

    @Override
    public ServerWebExchange processExchange(ServerWebExchange exchange) {

        // Load trace id from request or create new
        String traceId;
        if (forwardIncomingTrace()) {
            // make sure we take over the passed in traceparent when it is valid
            String header = exchange.getRequest().getHeaders().getFirst(getMainRequestHeader());
            traceId = applyExistingTrace(header, exchange);
        }
        else {
            traceId = establishNew();
        }

        // Add TraceID to exchange
        exchange.getAttributes().put(TraceContextFilter.CONTEXT_KEY, traceId);

        // Add trace if to downstream request
        if (forwardIncomingTrace()) {
            var mutatedRequest = exchange.getRequest().mutate().header(getMainRequestHeader(), traceId).build();
            exchange = exchange.mutate().request(mutatedRequest).build();
        }

        // Add trace id to response
        if (sendTraceResponse()) {
            logDebug(log, exchange, "Adding trace id to response");
            exchange.getResponse().getHeaders().put(getResponseHeader(), new ArrayList<>(Collections.singleton(traceId)));
        }

        return exchange;
    }

    public String establishNew() {
        return UUID.randomUUID().toString();
    }

    public String applyExistingTrace(String primaryTraceInfo, ServerWebExchange exchange) {
        if (primaryTraceInfo == null || primaryTraceInfo.length() < 4 || primaryTraceInfo.length() > config.getTraceProfile().getMaxLengthIncomingTrace()) {
            log.debug("No or to short/long traceId ({}) provided by caller, using my own instead.", SecureEncoder.encodeStringForLog(primaryTraceInfo, config.getTraceProfile().getMaxLengthIncomingTrace()));
            return establishNew();
        } else {
            log.debug("Applied incoming trace/correlation id.");
            return SecureEncoder.encodeStringForLog(primaryTraceInfo, config.getTraceProfile().getMaxLengthIncomingTrace());
        }
    }

    public String getMainRequestHeader() {
        return getHeaderName();
    }

    private String getHeaderName() {
        if (appliedHeaderName == null) {
            if (config.getTraceProfile().getTraceImplSpecificSettings() != null && config.getTraceProfile().getTraceImplSpecificSettings().containsKey(HEADER_NAME)) {
                if (config.getTraceProfile().getTraceImplSpecificSettings().get(HEADER_NAME) != null && config.getTraceProfile().getTraceImplSpecificSettings().get(HEADER_NAME) instanceof String) {
                    appliedHeaderName = (String) config.getTraceProfile().getTraceImplSpecificSettings().get(HEADER_NAME);
                } else {
                    log.warn("Invalid Header Name provided for trace/correlation logging in config, using default name instead.");
                    appliedHeaderName = DEFAULT_HEADER;
                }
            } else {
                log.debug("No custom headerName provided for trace/correlation logging in config. Using default.");
                appliedHeaderName = DEFAULT_HEADER;
            }
        }
        return appliedHeaderName;
    }

    public String getSecondaryRequestHeader() {
        return "n/a";
    }

    public String getResponseHeader() {
        return DEFAULT_HEADER;
    }

    public boolean forwardIncomingTrace() {
        return config.getTraceProfile().getForwardIncomingTrace();
    }

    public boolean acceptAdditionalTraceInfo() {
        return false;
    }

    public boolean sendTraceResponse() {
        return config.getTraceProfile().getSendTraceResponse();
    }
}
