package org.owasp.oag.services.login.drivers;

import java.net.URI;

public class LoginDriverResult {

    private URI authURI;
    private String state;

    public LoginDriverResult(URI authURI, String state) {
        this.authURI = authURI;
        this.state = state;
    }

    public URI getAuthURI() {
        return authURI;
    }

    public void setAuthURI(URI authURI) {
        this.authURI = authURI;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
