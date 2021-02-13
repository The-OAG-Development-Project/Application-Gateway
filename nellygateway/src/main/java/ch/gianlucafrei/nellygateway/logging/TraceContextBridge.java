package ch.gianlucafrei.nellygateway.logging;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.TraceProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * This class bridges to the concrete trace context implementation that was configured in the main config yaml (section traceProfile).
 */
@Component
@Primary
public class TraceContextBridge implements TraceContext {

    private final TraceContext implClass;

    @Autowired
    public TraceContextBridge(ApplicationContext context, NellyConfig config) {
        implClass = loadBean(context, config);
    }

    private TraceContext loadBean(ApplicationContext context, NellyConfig config) {
        TraceProfile traceProfile = config.getTraceProfile();
        TraceContext implClass = context.getBean(traceProfile.getType(), TraceContext.class);

        if (implClass == null) {
            throw new RuntimeException("Trace implementation class not found: " + traceProfile.getType());
        }
        return implClass;
    }

    @Override
    public void establishNew() {
        implClass.establishNew();
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        implClass.applyExistingTrace(primaryTraceInfo, secondaryTraceInfo);
    }

    @Override
    public void teardown() {
        implClass.teardown();
    }

    @Override
    public String getMainRequestHeader() {
        return implClass.getMainRequestHeader();
    }

    @Override
    public String getSecondaryRequestHeader() {
        return implClass.getSecondaryRequestHeader();
    }

    @Override
    public String getResponseHeader() {
        return implClass.getResponseHeader();
    }

    @Override
    public String getTraceString() {
        return implClass.getTraceString();
    }

    @Override
    public String getSecondaryTraceInfoString() {
        return implClass.getSecondaryTraceInfoString();
    }

    @Override
    public String getTraceResponseString() {
        return implClass.getTraceResponseString();
    }

    @Override
    public boolean hasCurrentTraceId() {
        return implClass.hasCurrentTraceId();
    }

    @Override
    public boolean forwardIncomingTrace() {
        return implClass.forwardIncomingTrace();
    }

    @Override
    public boolean acceptAdditionalTraceInfo() {
        return implClass.acceptAdditionalTraceInfo();
    }

    @Override
    public boolean sendTraceResponse() {
        return implClass.sendTraceResponse();
    }
}
