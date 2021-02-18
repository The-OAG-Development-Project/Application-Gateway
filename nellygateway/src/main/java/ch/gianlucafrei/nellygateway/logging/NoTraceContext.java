package ch.gianlucafrei.nellygateway.logging;

import org.springframework.stereotype.Component;

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

    @Override
    public void establishNew() {
        // nothing to do
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        // nothing to do
    }

    @Override
    public void teardown() {
        // nothing to do
    }

    @Override
    public String getMainRequestHeader() {
        return "n/a";
    }

    @Override
    public String getSecondaryRequestHeader() {
        return "n/a";
    }

    @Override
    public String getResponseHeader() {
        return "n/a";
    }

    @Override
    public String getTraceString() {
        return null;
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
    public boolean hasCurrentTraceId() {
        return false;
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
