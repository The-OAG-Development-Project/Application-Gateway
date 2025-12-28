package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured TraceProfile section in the config file.
 * This class represents the configuration for trace and correlation logging,
 * including settings for forwarding incoming trace information, accepting additional
 * trace information, and implementation-specific settings.
 * It also implements validation to ensure all required settings are provided.
 */
public class TraceProfile implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(TraceProfile.class);

    private Boolean forwardIncomingTrace;
    private Integer maxLengthIncomingTrace;
    private Boolean acceptAdditionalTraceInfo;
    private Integer maxLengthAdditionalTraceInfo;
    private Boolean sendTraceResponse;
    private String type;
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    /**
     * Default constructor for TraceProfile.
     * Creates an empty trace profile with default values.
     */
    public TraceProfile() {
    }

    /**
     * Parameterized constructor for TraceProfile.
     * 
     * @param forwardIncomingTrace Whether to forward incoming trace information
     * @param maxLengthIncomingTrace Maximum length allowed for incoming trace information
     * @param acceptAdditionalTraceInfo Whether to accept additional trace information
     * @param maxLengthAdditionalTraceInfo Maximum length allowed for additional trace information
     * @param sendTraceResponse Whether to send trace information in responses
     * @param type The type of trace implementation to use
     * @param implSpecificSettings Implementation-specific settings for the trace profile
     */
    public TraceProfile(Boolean forwardIncomingTrace, Integer maxLengthIncomingTrace, Boolean acceptAdditionalTraceInfo, Integer maxLengthAdditionalTraceInfo, Boolean sendTraceResponse, String type, Map<String, Object> implSpecificSettings) {
        this.forwardIncomingTrace = forwardIncomingTrace;
        this.maxLengthIncomingTrace = maxLengthIncomingTrace;
        this.acceptAdditionalTraceInfo = acceptAdditionalTraceInfo;
        this.maxLengthAdditionalTraceInfo = maxLengthAdditionalTraceInfo;
        this.sendTraceResponse = sendTraceResponse;
        this.type = type;
        this.implSpecificSettings = implSpecificSettings;
    }

    /**
     * Gets whether incoming trace information should be forwarded.
     * 
     * @return Boolean indicating whether to forward incoming trace information
     */
    public Boolean getForwardIncomingTrace() {
        return forwardIncomingTrace;
    }

    /**
     * Sets whether incoming trace information should be forwarded.
     * 
     * @param forwardIncomingTrace Boolean indicating whether to forward incoming trace information
     */
    public void setForwardIncomingTrace(Boolean forwardIncomingTrace) {
        this.forwardIncomingTrace = forwardIncomingTrace;
    }

    /**
     * Gets the maximum length allowed for incoming trace information.
     * 
     * @return Integer representing the maximum length for incoming trace information
     */
    public Integer getMaxLengthIncomingTrace() {
        return maxLengthIncomingTrace;
    }

    /**
     * Sets the maximum length allowed for incoming trace information.
     * 
     * @param maxLengthIncomingTrace Integer representing the maximum length for incoming trace information
     */
    public void setMaxLengthIncomingTrace(Integer maxLengthIncomingTrace) {
        this.maxLengthIncomingTrace = maxLengthIncomingTrace;
    }

    /**
     * Gets whether additional trace information should be accepted.
     * 
     * @return Boolean indicating whether to accept additional trace information
     */
    public Boolean getAcceptAdditionalTraceInfo() {
        return acceptAdditionalTraceInfo;
    }

    /**
     * Sets whether additional trace information should be accepted.
     * 
     * @param acceptAdditionalTraceInfo Boolean indicating whether to accept additional trace information
     */
    public void setAcceptAdditionalTraceInfo(Boolean acceptAdditionalTraceInfo) {
        this.acceptAdditionalTraceInfo = acceptAdditionalTraceInfo;
    }

    /**
     * Gets the maximum length allowed for additional trace information.
     * 
     * @return Integer representing the maximum length for additional trace information
     */
    public Integer getMaxLengthAdditionalTraceInfo() {
        return maxLengthAdditionalTraceInfo;
    }

    /**
     * Sets the maximum length allowed for additional trace information.
     * 
     * @param maxLengthAdditionalTraceInfo Integer representing the maximum length for additional trace information
     */
    public void setMaxLengthAdditionalTraceInfo(Integer maxLengthAdditionalTraceInfo) {
        this.maxLengthAdditionalTraceInfo = maxLengthAdditionalTraceInfo;
    }

    /**
     * Gets whether trace information should be sent in responses.
     * 
     * @return Boolean indicating whether to send trace information in responses
     */
    public Boolean getSendTraceResponse() {
        return sendTraceResponse;
    }

    /**
     * Sets whether trace information should be sent in responses.
     * 
     * @param sendTraceResponse Boolean indicating whether to send trace information in responses
     */
    public void setSendTraceResponse(Boolean sendTraceResponse) {
        this.sendTraceResponse = sendTraceResponse;
    }

    /**
     * Gets the type of trace implementation to use.
     * 
     * @return String representing the type of trace implementation
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of trace implementation to use.
     * 
     * @param type String representing the type of trace implementation
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the implementation-specific settings for the trace profile.
     * These settings are specific to the trace implementation type specified in the configuration.
     * 
     * @return A map containing implementation-specific settings
     */
    public Map<String, Object> getImplSpecificSettings() {
        return implSpecificSettings;
    }

    /**
     * Sets the implementation-specific settings for the trace profile.
     * These settings are specific to the trace implementation type specified in the configuration.
     * 
     * @param implSpecificSettings A map containing implementation-specific settings
     */
    public void setTraceImplSpecificSettings(Map<String, Object> implSpecificSettings) {
        this.implSpecificSettings = implSpecificSettings;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {

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
