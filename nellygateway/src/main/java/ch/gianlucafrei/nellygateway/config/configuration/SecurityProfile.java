package ch.gianlucafrei.nellygateway.config.configuration;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class SecurityProfile {

    private List<String> allowedMethods;
    private String csrfProtection;
    private List<String> csrfSafeMethods = Lists.asList("GET", new String[]{"HEAD", "OPTIONS"});
    private Map<String, String> responseHeaders;


    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getCsrfProtection() {
        return csrfProtection;
    }

    public void setCsrfProtection(String csrfProtection) {
        this.csrfProtection = csrfProtection;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    private void setResponseHeaders(Map<String, String> headers) {
        this.responseHeaders = headers;
    }

    public List<String> getCsrfSafeMethods() {

        return this.csrfSafeMethods;
    }

    public void setCsrfSafeMethods(List<String> csrfSafeMethods) {

        this.csrfSafeMethods = csrfSafeMethods;
    }
}
