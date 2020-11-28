package ch.gianlucafrei.nellygateway.config.configuration;

import java.util.ArrayList;
import java.util.Map;

public class SecurityProfile {

    private ArrayList<String> allowedMethods;
    private String csrfProtection;
    private Map<String, String> responseHeaders;


    public ArrayList<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(ArrayList<String> allowedMethods) {
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
}
