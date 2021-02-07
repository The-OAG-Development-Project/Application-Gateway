package ch.gianlucafrei.nellygateway.logging.w3ctrace;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.logging.TraceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implements the W3C Trace Context specification.
 * See https://w3c.github.io/trace-context/
 * <p>
 * It provides functionality to support trace filters (such as TraceContextFilter) and the log framework.
 */
@Component("w3cTrace")
public class W3cTraceContext implements TraceContext {
    private static final String W3C_MAIN_RESPONSE_HEADER_NAME = "traceresponse";
    private static final String W3C_MAIN_HEADER_NAME = "traceparent";
    private static final String W3C_SUB_HEADER_NAME = "tracestate";

    private static final Logger log = LoggerFactory.getLogger(W3cTraceContext.class);

    private static ThreadLocal<W3cTraceContextState> currentContext = new ThreadLocal<>();

    //TODO: make sure MDC is really in log files.
    //TODO: Update documentation (on web) with new settings
    @Autowired
    private NellyConfig config;


    /**
     * Establishes a new CorrelationId in the system. This is typically used when a new request hits the OAGW.
     * Make sure you also call teardown when the call/scope is finished.
     */
    @Override
    public void establishNew() {
        W3cTraceContextState traceCtx = new W3cTraceContextState();
        currentContext.set(traceCtx);
        log.info("Started new trace for request.");
    }

    /**
     * cleanup everything and remove the current context
     */
    @Override
    public void teardown() {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            log.info("Ending trace for request");
            state.teardown();
            currentContext.remove();
        } else {
            log.warn("Teardown of W3CTraceContext without having a context setup prior. Check for developer error in code.");
        }
    }

    /**
     * @return the header that should be used when a client sends a correlationId
     */
    @Override
    public String getMainRequestHeader() {
        return W3C_MAIN_HEADER_NAME;
    }

    /**
     * @return the header that should be used when a client sends secondary information (i.e. vendor specific) for a trace
     */
    @Override
    public String getSecondaryRequestHeader() {
        return W3C_SUB_HEADER_NAME;
    }

    /**
     * @return the header that should be used when we respond the correlation id upstream
     */
    @Override
    public String getResponseHeader() {
        return W3C_MAIN_RESPONSE_HEADER_NAME;
    }

    /**
     * @return the current correlation id as a string. in the case of W3CTraceContext this is the traceparent header value.
     */
    @Override
    public String getTraceString() {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            return state.getTraceString();
        }

        throw new TraceException("Requested to get Trace String but failed to setup trace prior execution.");
    }

    /**
     * @return the headername traceparent as specified by W3C.
     */
    @Override
    public String getSecondaryTraceInfoString() {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            return state.getSecondaryTraceInfoString();
        }

        throw new TraceException("Requested to get Secondary Trace String but failed to setup trace prior execution.");
    }

    @Override
    public String getTraceResponseString() {
        return getTraceString();
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        if (!hasCurrentTraceId()) {
            establishNew();
        }
        String oldTraceId = getTraceString();
        //TODO: implement trace id change based on provided data, consider to long headers

        log.info("Switched trace id {} to this new one.", oldTraceId);
    }

    @Override
    public boolean hasCurrentTraceId() {
        return currentContext.get() != null;
    }

    /**
     * @return true when a passed in traceparent should be re-used and passed on downstream
     */
    @Override
    public boolean forwardIncomingTrace() {
        return config.getTraceProfile().getForwardIncomingTrace();
    }

    /**
     * @return true when also the tracestate passed in should be forwarded downstream
     */
    @Override
    public boolean acceptAdditionalTraceInfo() {
        return config.getTraceProfile().getAcceptAdditionalTraceInfo();
    }

    /**
     * @return true when we send the caller the correlation id (traceparent) we used
     */
    @Override
    public boolean sendTraceResponse() {
        return config.getTraceProfile().getSendTraceResponse();
    }

    @Override
    public int hashCode() {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            return state.hashCode();
        }

        return 0;
    }

    @Override
    public boolean equals(Object other) {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            return state.equals(other);
        }

        if (other == null) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        W3cTraceContextState state = currentContext.get();

        if (state != null) {
            return state.toString();
        }

        return "W3CTraceContext - No Trace Context setup";
    }
}
