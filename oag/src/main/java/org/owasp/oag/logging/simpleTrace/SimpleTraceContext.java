package org.owasp.oag.logging.simpleTrace;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.utils.SecureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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

    private String appliedHeaderName = null;

    private String traceId;

    @Autowired
    private MainConfig config;

    @Override
    public void generateNewTraceId() {
        traceId = UUID.randomUUID().toString();
    }

    @Override
    public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
        if (primaryTraceInfo == null || primaryTraceInfo.length() < 4 || primaryTraceInfo.length() > config.getTraceProfile().getMaxLengthIncomingTrace()) {
            generateNewTraceId();
            log.debug("Passed in trace id: {} could not be applied. Using this new one instead: {}.", SecureEncoder.encodeStringForLog(primaryTraceInfo, 128), getTraceString());
        } else {
            traceId = primaryTraceInfo;
            log.debug("Applied incoming trace/correlation id: {}", getTraceString());
        }
    }

    @Override
    public boolean sendTraceDownstream() {
        return true;
    }

    @Override
    public String getMainRequestHeader() {
        return getHeaderName();
    }

    @Override
    public String getSecondaryRequestHeader() {
        return "n/a";
    }

    @Override
    public String getResponseHeader() {
        return getHeaderName();
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
        return getTraceString();
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


    private String getHeaderName() {
        if (appliedHeaderName == null) {
            if (config.getTraceProfile().getImplSpecificSettings() != null && config.getTraceProfile().getImplSpecificSettings().containsKey(HEADER_NAME)) {
                if (config.getTraceProfile().getImplSpecificSettings().get(HEADER_NAME) != null && config.getTraceProfile().getImplSpecificSettings().get(HEADER_NAME) instanceof String) {
                    appliedHeaderName = (String) config.getTraceProfile().getImplSpecificSettings().get(HEADER_NAME);
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
}
