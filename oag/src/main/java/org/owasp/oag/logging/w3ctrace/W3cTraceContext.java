package org.owasp.oag.logging.w3ctrace;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.spring.TraceContextFilter;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.logging.TraceException;
import org.owasp.oag.utils.LoggingUtils;
import org.owasp.oag.utils.SecureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class W3cTraceContext implements TraceContext {
    private static final String W3C_MAIN_RESPONSE_HEADER_NAME = "traceresponse";
    private static final String W3C_MAIN_HEADER_NAME = "traceparent";
    private static final String W3C_SUB_HEADER_NAME = "tracestate";

    private static final Logger log = LoggerFactory.getLogger(W3cTraceContext.class);

    private W3cTraceContextState state;

    @Autowired
    private MainConfig config;

    @Override
    public void generateNewTraceId() {
        state = new W3cTraceContextState();
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        try {
            state = new W3cTraceContextState(primaryTraceInfo, secondaryTraceInfo, config.getTraceProfile().getMaxLengthAdditionalTraceInfo());
        } catch(Exception e){
            // could not take over data, making sure we have anyway a trace id. Reason is logged din state.
            generateNewTraceId();
            log.info("Passed in trace id: {} could not be applied. Using this new one instead: {}.", SecureEncoder.encodeStringForLog(primaryTraceInfo, 128), getTraceString());
        }
    }

    @Override
    public boolean sendTraceDownstream() {
        return true;
    }

    @Override
    public String getTraceString() {
        return state.getTraceString();
    }

    @Override
    public String getSecondaryTraceInfoString() {
        return state.getSecondaryTraceInfoString();
    }

    @Override
    public String getTraceResponseString() {
        return state.getTraceString();
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
}
