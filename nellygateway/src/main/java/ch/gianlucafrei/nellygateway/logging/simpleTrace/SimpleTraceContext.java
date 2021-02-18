package ch.gianlucafrei.nellygateway.logging.simpleTrace;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.logging.TraceException;
import ch.gianlucafrei.nellygateway.utils.SecureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    private static volatile String appliedHeaderName = null;

    @Autowired
    private NellyConfig config;

    @Override
    public void establishNew() {
        MDC.put(MDC_CORRELATION_ID_KEY, UUID.randomUUID().toString());
        log.info("Started new trace for request.");
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        if (primaryTraceInfo == null || primaryTraceInfo.length() < 4 || primaryTraceInfo.length() > config.getTraceProfile().getMaxLengthIncomingTrace()) {
            establishNew();
            log.info("No or to short/long traceId ({}) provided by caller, using my own instead.", SecureEncoder.encodeStringForLog(primaryTraceInfo, config.getTraceProfile().getMaxLengthIncomingTrace()));
        } else {
            MDC.put(MDC_CORRELATION_ID_KEY, SecureEncoder.encodeStringForLog(primaryTraceInfo, config.getTraceProfile().getMaxLengthIncomingTrace()));
            log.info("Applied incoming trace/correlation id.");
        }
    }

    @Override
    public void teardown() {
        if (hasCurrentTraceId()) {
            log.info("Ending trace for request");
            MDC.remove(MDC_CORRELATION_ID_KEY);
        } else {
            log.warn("Teardown of SimpleTraceContext without having a context setup prior. Check for developer error in code.");
        }

    }

    @Override
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

    @Override
    public String getSecondaryRequestHeader() {
        return "n/a";
    }

    @Override
    public String getResponseHeader() {
        return MDC.get(MDC_CORRELATION_ID_KEY);
    }

    @Override
    public String getTraceString() {
        if (hasCurrentTraceId()) {
            return MDC.get(MDC_CORRELATION_ID_KEY);
        }
        throw new TraceException("Requested to get Trace String but failed to setup trace prior execution.");
    }

    @Override
    public String getSecondaryTraceInfoString() {
        return null;
    }

    @Override
    public String getTraceResponseString() {
        return getMainRequestHeader();
    }

    @Override
    public boolean hasCurrentTraceId() {
        return MDC.get(MDC_CORRELATION_ID_KEY) != null;
    }

    @Override
    public boolean forwardIncomingTrace() {
        return config.getTraceProfile().getForwardIncomingTrace();
    }

    @Override
    public boolean acceptAdditionalTraceInfo() {
        return false;
    }

    @Override
    public boolean sendTraceResponse() {
        return config.getTraceProfile().getSendTraceResponse();
    }
}
