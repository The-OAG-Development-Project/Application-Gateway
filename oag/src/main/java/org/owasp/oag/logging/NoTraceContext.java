package org.owasp.oag.logging;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implements a no-op Trace / Correlation Id. Effectively disabling correlation-Logging.
 * (Only uses the traceId OAG internally).
 * Configue in the main config with:
 * <code>
 * traceProfile:
 * type: noTraceContext
 * </code>
 * It does not support any traceImplSpecificSettings and ignores all other settings in the traceProfile.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NoTraceContext implements TraceContext {

    private final String traceId = UUID.randomUUID().toString();

    @Override
    public void generateNewTraceId() {
        // nothing to do
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        // nothing to do
    }

    @Override
    public boolean sendTraceDownstream() {
        return false;
    }

    @Override
    public String getMainRequestHeader() {
        return null;
    }

    @Override
    public String getSecondaryRequestHeader() {
        return null;
    }

    @Override
    public String getResponseHeader() {
        return null;
    }

    @Override
    public String getTraceString() {
        return traceId;
    }

    @Override
    public String getSecondaryTraceInfoString() {
        return null;
    }

    @Override
    public String getTraceResponseString() {
        return null;
    }

    @Override
    public boolean forwardIncomingTrace() {
        return false;
    }

    @Override
    public boolean acceptAdditionalTraceInfo() {
        return false;
    }

    @Override
    public boolean sendTraceResponse() {
        return false;
    }
}
