package ch.gianlucafrei.nellygateway.config.configuration;

import java.util.Map;

public class SecurityProfile {

    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
