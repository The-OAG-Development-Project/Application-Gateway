package org.owasp.oag.config.configuration;

import org.owasp.oag.config.Subconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured TraceProfile section in the config file
 */
public class TraceProfile implements Subconfig {

    private static final Logger log = LoggerFactory.getLogger(TraceProfile.class);

    private Boolean forwardIncomingTrace;
    private Integer maxLengthIncomingTrace;
    private Boolean acceptAdditionalTraceInfo;
    private Integer maxLengthAdditionalTraceInfo;
    private Boolean sendTraceResponse;
    private String type;
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    public TraceProfile() {
    }

    public TraceProfile(Boolean forwardIncomingTrace, Integer maxLengthIncomingTrace, Boolean acceptAdditionalTraceInfo, Integer maxLengthAdditionalTraceInfo, Boolean sendTraceResponse, String type, Map<String, Object> implSpecificSettings) {
        this.forwardIncomingTrace = forwardIncomingTrace;
        this.maxLengthIncomingTrace = maxLengthIncomingTrace;
        this.acceptAdditionalTraceInfo = acceptAdditionalTraceInfo;
        this.maxLengthAdditionalTraceInfo = maxLengthAdditionalTraceInfo;
        this.sendTraceResponse = sendTraceResponse;
        this.type = type;
        this.implSpecificSettings = implSpecificSettings;
    }

    public Boolean getForwardIncomingTrace() {
        return forwardIncomingTrace;
    }

    public void setForwardIncomingTrace(Boolean forwardIncomingTrace) {
        this.forwardIncomingTrace = forwardIncomingTrace;
    }

    public Integer getMaxLengthIncomingTrace() {
        return maxLengthIncomingTrace;
    }

    public void setMaxLengthIncomingTrace(Integer maxLengthIncomingTrace) {
        this.maxLengthIncomingTrace = maxLengthIncomingTrace;
    }

    public Boolean getAcceptAdditionalTraceInfo() {
        return acceptAdditionalTraceInfo;
    }

    public void setAcceptAdditionalTraceInfo(Boolean acceptAdditionalTraceInfo) {
        this.acceptAdditionalTraceInfo = acceptAdditionalTraceInfo;
    }

    public Integer getMaxLengthAdditionalTraceInfo() {
        return maxLengthAdditionalTraceInfo;
    }

    public void setMaxLengthAdditionalTraceInfo(Integer maxLengthAdditionalTraceInfo) {
        this.maxLengthAdditionalTraceInfo = maxLengthAdditionalTraceInfo;
    }

    public Boolean getSendTraceResponse() {
        return sendTraceResponse;
    }

    public void setSendTraceResponse(Boolean sendTraceResponse) {
        this.sendTraceResponse = sendTraceResponse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getImplSpecificSettings() {
        return implSpecificSettings;
    }

    public void setTraceImplSpecificSettings(Map<String, Object> implSpecificSettings) {
        this.implSpecificSettings = implSpecificSettings;
    }

    @Override
    public List<String> getErrors(ApplicationContext context, MainConfig rootConfig) {

        var errors = new ArrayList<String>();

        if (forwardIncomingTrace == null)
            errors.add("'forwardIncomingTrace' not specified");

        if (maxLengthIncomingTrace == null)
            errors.add("'maxLengthIncomingTrace' not specified");

        if (acceptAdditionalTraceInfo == null)
            errors.add("'acceptAdditionalTraceInfo' not specified");

        if (maxLengthAdditionalTraceInfo == null)
            errors.add("'maxLengthAdditionalTraceInfo' not specified");

        if (sendTraceResponse == null)
            errors.add("'sendTraceResponse' not specified");

        if (type == null)
            errors.add("'type' not specified. Must be the bean name of a TraceContext implementation such as w3cTrace. Specify 'noTrace' to disable correlation Logging.");

        if (context != null && !context.containsBean(type)) {
            errors.add("Specified type '" + type + "' does not match a trace/correlation log implementation. Must be the bean name of a TraceContext implementation such as w3cTrace. Specify 'noTrace' to disable correlation Logging.");
        } else {
            log.info("Using log trace / correlation implementation of {}.", type);
        }

        return errors;
    }
}
